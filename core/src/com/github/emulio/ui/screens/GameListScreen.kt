package com.github.emulio.ui.screens

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.files.FileHandle
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.Pixmap
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator
import com.badlogic.gdx.math.Interpolation
import com.badlogic.gdx.scenes.scene2d.Actor
import com.badlogic.gdx.scenes.scene2d.Group
import com.badlogic.gdx.scenes.scene2d.InputEvent
import com.badlogic.gdx.scenes.scene2d.actions.Actions
import com.badlogic.gdx.scenes.scene2d.actions.SequenceAction
import com.badlogic.gdx.scenes.scene2d.actions.TemporalAction
import com.badlogic.gdx.scenes.scene2d.ui.*
import com.badlogic.gdx.scenes.scene2d.ui.List
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable
import com.badlogic.gdx.utils.Align
import com.badlogic.gdx.utils.Scaling
import com.badlogic.gdx.utils.Timer
import com.github.emulio.Emulio
import com.github.emulio.model.Game
import com.github.emulio.model.Platform
import com.github.emulio.model.theme.*
import com.github.emulio.process.ProcessException
import com.github.emulio.process.ProcessLauncher
import com.github.emulio.ui.input.InputListener
import com.github.emulio.ui.input.InputManager
import com.github.emulio.utils.DateHelper
import com.github.emulio.utils.translate
import mu.KotlinLogging
import java.io.File


class GameListScreen(emulio: Emulio, val platform: Platform) : EmulioScreen(emulio), InputListener {

    val logger = KotlinLogging.logger { }

	private val inputController: InputManager = InputManager(this, emulio.config, stage)
	private val interpolation = Interpolation.fade

    private var games: kotlin.collections.List<Game>
    private lateinit var filteredGames: kotlin.collections.List<Game>

    private var selectedGame: Game? = null

    private lateinit var listView: List<String>
    private lateinit var listScrollPane: ScrollPane

    private lateinit var descriptionScrollPane: ScrollPane
    private lateinit var gameImage: Image
    private lateinit var gameReleaseDate: TextField
    private lateinit var gameDescription: Label
    private lateinit var gamePlayCount: TextField
    private lateinit var gameLastPlayed: TextField
    private lateinit var gamePlayers: TextField
    private lateinit var gameGenre: TextField
    private lateinit var gamePublisher: TextField
    private lateinit var gameDeveloper: TextField
    private lateinit var root: Group
    private lateinit var logo: Image
    private lateinit var imageView: ViewImage
    private lateinit var ratingImages: GameListScreen.RatingImages

    private var lastTimer: Timer.Task? = null
    private var lastSequenceAction: SequenceAction? = null
    private var needSelectionView: Boolean = false

    init {
		Gdx.input.inputProcessor = inputController

		val gamesFound = emulio.games!![platform]?.toList() ?: emptyList()

        val supportedExtensions = platform.romsExtensions
        val gamesMap = mutableMapOf<String, Game>()
        gamesFound.forEach { game ->

            val gameFound: Game? = gamesMap[game.path.nameWithoutExtension]
            if (gameFound != null) {
                if (gameFound.path.name != game.path.nameWithoutExtension) {
                    gamesMap[game.path.nameWithoutExtension] = game
                    game.displayName = game.path.nameWithoutExtension

                } else if (gameFound.path.name == game.path.nameWithoutExtension) {

                    val idxFound = supportedExtensions.indexOf(gameFound.path.extension)
                    val idxGame = supportedExtensions.indexOf(game.path.extension)

                    if (idxGame < idxFound) {
                        val key = game.name!!

                        gamesMap[key] = game
                        game.displayName = key
                    }
                }
            } else {
                val key = game.name ?: game.path.name
                game.displayName = key
                gamesMap[key] = game

            }
        }

        games = gamesMap.values.sortedBy { it.displayName!!.toLowerCase() }
        needSelectionView = games.size > emulio.config.maxGamesList &&
                emulio.config.maxGamesList != -1

		initGUI()
	}

    private fun isBasicViewOnly(): Boolean {
        return games.none { it.id != null || it.description != null || it.image != null }
    }

    private var guiready: Boolean = false

    private fun initGUI() {
		val theme = emulio.theme[platform]!!
        val basicViewOnly = isBasicViewOnly()

        val view = theme.findView(if (basicViewOnly) "basic" else "detailed")!!

        buildCommonComponents(view)

        if (basicViewOnly) {
            buildBasicView(view)
        } else {
            buildDetailedView(view)
        }
	}

    override fun onScreenLoad() {
        logger.debug { "onScreenLoad" }
        guiready = true

        if (games.size > 1) {
            listView.selectedIndex = 0
            if (!needSelectionView) {
                selectedGame = games[0]
                updateGameSelected()
            }
        }
    }

    private fun buildBasicView(basicView: View) {

		gamelistView = basicView.findViewItem("gamelist") as TextList
        buildListScrollPane { buildListView() }

	}

    private var lastSelectedIndex: Int = -1

    private fun buildListScrollPane(builder: () -> List<String>) {
        listView = builder()

        listView.addListener(object : ClickListener() {
            override fun clicked(event: InputEvent?, x: Float, y: Float) {
                val newIndex = listView.selectedIndex

                if (lastSelectedIndex == newIndex) {
                    onConfirmButton()
                    return
                }

                if (!isSelectionListView) {
                    selectedGame = if (needSelectionView) {
                        filteredGames[newIndex]
                    } else {
                        games[newIndex]
                    }
                    updateGameSelected()
                }
                
                lastSelectedIndex = listView.selectedIndex
            }
        })

        listScrollPane = ScrollPane(listView, ScrollPane.ScrollPaneStyle().apply {

        }).apply {

            setFlickScroll(true)
            setScrollBarPositions(false, true)

            setScrollingDisabled(true, false)
            setSmoothScrolling(true)

            isTransform = true

            setSize(gamelistView)
            setPosition(gamelistView)
        }

        stage.addActor(listScrollPane)
    }

    private fun buildCommonComponents(view: View) {
		val backgroundView = view.findViewItem("background") as ViewImage?
		if (backgroundView != null) {
			stage.addActor(buildImage(backgroundView).apply {
				setScaling(Scaling.stretch)
				setPosition(0f, 0f)
				setSize(screenWidth, screenHeight)
			})
		} else {
			val lightGrayTexture = createColorTexture(0xc5c6c7FF.toInt())
			stage.addActor(Image(lightGrayTexture).apply {
				setFillParent(true)
			})
		}

		val footer = view.findViewItem("footer") as ViewImage?
        val imgFooter: Image?

        if (footer != null) {
            imgFooter = buildImage(footer, Scaling.stretch)
            stage.addActor(imgFooter)
		} else {
            imgFooter = null
        }

		val header = view.findViewItem("header") as ViewImage?
		if (header != null) {
			stage.addActor(buildImage(header, Scaling.stretch))
		}

		initRoot()
		initLogoSmall()

		val systemName1 = view.findViewItem("system_name_1")?.let { it as Text }
		if (systemName1 != null) {
			stage.addActor(buildTextField(systemName1))
		}

		val systemName2 = view.findViewItem("system_name_2")?.let { it as Text }
		if (systemName2 != null) {
			stage.addActor(buildTextField(systemName2))
		}

		val logo = view.findViewItem("logo") as ViewImage?
		if (logo != null) {
			stage.addActor(buildImage(logo))
		}

        val footerHeight: Float
        val footerY: Float
        if (imgFooter != null) {
            footerHeight = imgFooter.height
            footerY = imgFooter.y
        } else {
            footerHeight = 10f
            footerY = 10f
        }


        buildHelpHuds(footerY, footerHeight)
	}

    private lateinit var gamelistView: TextList

    private fun buildDetailedView(detailedView: View) {

        val descriptionView = detailedView.findViewItem("md_description") as Text?
        if (descriptionView != null) {

            gameDescription = buildLabel(descriptionView)
            gameDescription.setWrap(true)

            descriptionScrollPane = ScrollPane(gameDescription, ScrollPane.ScrollPaneStyle()).apply {
                setFlickScroll(true)
                setScrollBarPositions(false, true)

                setSmoothScrolling(true)
                setForceScroll(false, true)

                isTransform = true

                setSize(descriptionView)
                setPosition(descriptionView)
            }

            stage.addActor(descriptionScrollPane)
        }

        gamelistView = detailedView.findViewItem("gamelist") as TextList
        buildListScrollPane { buildListView() }

        val imageView = detailedView.findViewItem("md_image") as ViewImage?
        if (imageView != null) {
            gameImage = buildImage(imageView)
            stage.addActor(gameImage)
            this.imageView = imageView
        }

        val lbRating = buildLabel(detailedView, "md_lbl_rating", "Rating:")
        buildLabel(detailedView, "md_lbl_releasedate", "Released:")
        buildLabel(detailedView, "md_lbl_developer", "Developer:")
        buildLabel(detailedView, "md_lbl_publisher", "Publisher:")
        buildLabel(detailedView, "md_lbl_genre", "Genre:")
        buildLabel(detailedView, "md_lbl_players", "Players:")
        buildLabel(detailedView, "md_lbl_lastplayed", "Last played:")
        buildLabel(detailedView, "md_lbl_playcount", "Times played:")


        val playCountView = detailedView.findViewItem("md_playcount") as Text?
        if (playCountView != null) {
            gamePlayCount = buildTextField(playCountView)
            stage.addActor(gamePlayCount)
        }

        val lastPlayedView = detailedView.findViewItem("md_lastplayed") as Text?
        if (lastPlayedView != null) {
            gameLastPlayed = buildTextField(lastPlayedView)
            stage.addActor(gameLastPlayed)
        }

        val playersView = detailedView.findViewItem("md_players") as Text?
        if (playersView != null) {
            gamePlayers = buildTextField(playersView)
            stage.addActor(gamePlayers)
        }

        val genreView = detailedView.findViewItem("md_genre") as Text?
        if (genreView != null) {
            gameGenre = buildTextField(genreView)
            stage.addActor(gameGenre)
        }

        val publisherView = detailedView.findViewItem("md_publisher") as Text?
        if (publisherView != null) {
            gamePublisher = buildTextField(publisherView)
            stage.addActor(gamePublisher)
        }

        val developerView = detailedView.findViewItem("md_developer") as Text?
        if (developerView != null) {
            gameDeveloper = buildTextField(developerView)
            stage.addActor(gameDeveloper)
        }

        val releaseDateView = detailedView.findViewItem("md_releasedate") as Text?
        if (releaseDateView != null) {
            gameReleaseDate = buildTextField(releaseDateView)
            stage.addActor(gameReleaseDate)
        }

        val ratingView = detailedView.findViewItem("md_rating") as Rating?
        if (ratingView != null) {
            buildRatingImages(ratingView, lbRating!!)
        }

	}

    data class RatingImages(
        val ratingImg1: Image,
        val ratingImg2: Image,
        val ratingImg3: Image,
        val ratingImg4: Image,
        val ratingImg5: Image,
        val ratingUnFilledTexture: Texture,
        val ratingFilledTexture: Texture,
        val ratingColor: Color
    )



    private fun buildRatingImages(ratingView: Rating, lbRating: Label) {
        val ratingTexture = buildTexture("images/resources/star_unfilled_128_128.png")

        val ratingFilledTexture = buildTexture("images/resources/star_filled_128_128.png")

        val ratingWidth = lbRating.height
        val ratingHeight = lbRating.height

        val ratingImg1 = Image(ratingTexture).apply {
            setSize(ratingWidth, ratingHeight)
            val (viewX, viewY) = getPosition(ratingView)

            x = viewX
            y = viewY + 6f
        }

        stage.addActor(ratingImg1)
        val ratingImg2 = buildImage(ratingTexture, ratingWidth, ratingHeight, ratingImg1.x + ratingImg1.width, ratingImg1.y)
        stage.addActor(ratingImg2)
        val ratingImg3 = buildImage(ratingTexture, ratingWidth, ratingHeight, ratingImg2.x + ratingImg2.width, ratingImg1.y)
        stage.addActor(ratingImg3)
        val ratingImg4 = buildImage(ratingTexture, ratingWidth, ratingHeight, ratingImg3.x + ratingImg3.width, ratingImg1.y)
        stage.addActor(ratingImg4)
        val ratingImg5 = buildImage(ratingTexture, ratingWidth, ratingHeight, ratingImg4.x + ratingImg4.width, ratingImg1.y)
        stage.addActor(ratingImg5)

        val color = getColor(ratingView.color)

        ratingImages = RatingImages(
            ratingImg1,
            ratingImg2,
            ratingImg3,
            ratingImg4,
            ratingImg5,
            ratingTexture,
            ratingFilledTexture,
            color
        )
    }

    private fun buildLabel(detailedView: View, viewName: String, viewText: String): Label? {
        val lbView = detailedView.findViewItem(viewName) as Text?
        if (lbView != null) {
            val lbl = buildLabel(lbView).apply {
                setText(viewText)
            }
            stage.addActor(lbl)

            return lbl
        }
        return null
    }

    private fun buildImage(image: ViewImage, scaling: Scaling = Scaling.fit, imagePath: File? = image.path): Image {
        val texture = if (imagePath != null) {
            Texture(FileHandle(imagePath), true)
        } else {
            val size = getSize(image)
            createColorTexture(0xFFCC00FF.toInt(), size.first.toInt(), size.second.toInt())
        }

		texture.setFilter(Texture.TextureFilter.MipMap, Texture.TextureFilter.MipMap)

		return Image(texture).apply {
			setScaling(scaling)

			setSize(image)
			setPosition(image)
			setOrigin(image)


            isVisible = imagePath != null
		}
	}

	private fun buildTextField(textView: Text): TextField {

		val text = if (textView.forceUpperCase) {
			textView.text?.toUpperCase() ?: ""
		} else {
			textView.text ?: ""
		}

        val color = getColor(textView.textColor ?: textView.color)

		return TextField(text, TextField.TextFieldStyle().apply {
            font = getFont(getFontPath(textView), getFontSize(textView.fontSize), color)
            fontColor = color
        }).apply {
			setAlignment(when(textView.alignment) {
				TextAlignment.LEFT -> Align.left
				TextAlignment.RIGHT -> Align.right
				TextAlignment.CENTER -> Align.center
				TextAlignment.JUSTIFY -> Align.left //TODO
			})
			setSize(textView)
			setPosition(textView)
		}
	}

    private fun buildLabel(textView: Text): Label {
        val text = if (textView.forceUpperCase) {
            textView.text?.toUpperCase() ?: ""
        } else {
            textView.text ?: ""
        }

        val lbColor = getColor(textView.textColor ?: textView.color)
        val font = getFont(getFontPath(textView), getFontSize(textView.fontSize))

        return Label(text, Label.LabelStyle(font, lbColor)).apply {
            setAlignment(Align.topLeft)
            setSize(textView)
            setPosition(textView)
            color = lbColor
        }
    }

	private fun buildListView(): List<String> {
        return if (needSelectionView) {
            buildSelectionListView()
        } else {
            buildGameListView(gamelistView, games)
        }
	}

    private fun buildSelectionListView(): List<String> {
        isSelectionListView = true

        return List<String>(List.ListStyle().apply {
            fontColorUnselected = getColor(gamelistView.primaryColor)
            fontColorSelected = getColor(gamelistView.selectedColor)
            font = getFont(getFontPath(gamelistView), getFontSize(gamelistView.fontSize))

            val selectorTexture = createColorTexture(Integer.parseInt(gamelistView.selectorColor + "FF", 16))
            selection = TextureRegionDrawable(TextureRegion(selectorTexture))

        }).apply {

            setSize(gamelistView)

            listOf(
                    "Games starting with Numbers...",
                    "Games starting with A...",
                    "Games starting with B...",
                    "Games starting with C...",
                    "Games starting with D...",
                    "Games starting with E...",
                    "Games starting with F...",
                    "Games starting with G...",
                    "Games starting with H...",
                    "Games starting with I...",
                    "Games starting with J...",
                    "Games starting with K...",
                    "Games starting with L...",
                    "Games starting with M...",
                    "Games starting with N...",
                    "Games starting with O...",
                    "Games starting with P...",
                    "Games starting with Q...",
                    "Games starting with R...",
                    "Games starting with S...",
                    "Games starting with T...",
                    "Games starting with U...",
                    "Games starting with V...",
                    "Games starting with W...",
                    "Games starting with X...",
                    "Games starting with Y...",
                    "Games starting with Z..."
            ).forEach { items.add(it) }


        }
    }

    private var isSelectionListView: Boolean = false

    private fun buildGameListView(gamelistView: TextList, selectedGames: kotlin.collections.List<Game>): List<String> {
        isSelectionListView = false

        return List<String>(List.ListStyle().apply {
            fontColorUnselected = getColor(gamelistView.primaryColor)
            fontColorSelected = getColor(gamelistView.selectedColor)
            font = getFont(getFontPath(gamelistView), getFontSize(gamelistView.fontSize))

            val selectorTexture = createColorTexture(Integer.parseInt(gamelistView.selectorColor + "FF", 16))
            selection = TextureRegionDrawable(TextureRegion(selectorTexture))

        }).apply {
            setSize(gamelistView)

            selectedGames.forEach { game ->
                items.add(game.name ?: game.path.name)
            }
        }
    }

    private fun Widget.setOrigin(viewItem: ViewItem) {
		if (viewItem.originX != null && viewItem.originY != null) {
			val originX = viewItem.originX!!
			val originY = viewItem.originY!!

			val offsetX = if (originX == 0f) {
				0f
			} else {
				width * originX
			}

			val offsetY = when (originY) {
                0f -> 0f
                1f -> height
                else -> height * (1f - viewItem.originY!!)
            }

			setOrigin(offsetX, offsetY)

			x += offsetX
			y += offsetY
		}
	}

    private fun getSize(viewItem: ViewItem): Pair<Float, Float> {
        var width = if (viewItem.sizeX != null) {
            screenWidth * viewItem.sizeX!!
        } else {
            200f
        }

        var height = if (viewItem.sizeY != null) {
            screenHeight * viewItem.sizeY!!
        } else {
            200f
        }

        if (viewItem.maxSizeX != null) {
            width = Math.max(width, screenWidth * viewItem.maxSizeX!!)
        }
        if (viewItem.maxSizeY != null) {
            height = Math.max(height, screenHeight * viewItem.maxSizeY!!)
        }
        return Pair(width, height)
    }

	private fun Actor.setSize(viewItem: ViewItem) {
		var width = if (viewItem.sizeX != null) {
			screenWidth * viewItem.sizeX!!
		} else {
			this.width
		}

		var height = if (viewItem.sizeY != null) {
			screenHeight * viewItem.sizeY!!
		} else {
			this.height
		}

		if (viewItem.maxSizeX != null) {
			width = Math.min(width, screenWidth * viewItem.maxSizeX!!)
		}
		if (viewItem.maxSizeY != null) {
			height = Math.min(height, screenHeight * viewItem.maxSizeY!!)
		}
		setSize(width, height)
	}

	private fun Actor.setPosition(view: ViewItem) {
        val (x, y) = getPosition(view)

		setPosition(x, y)
	}

    private fun Actor.getPosition(view: ViewItem): Pair<Float, Float> {
        val x = screenWidth * view.positionX!!
        val y = (screenHeight * (1f - view.positionY!!)) - height
        return Pair(x, y)
    }

    private fun getFontPath(textView: Text): FileHandle {
        return if (textView.fontPath != null) {
            FileHandle(textView.fontPath!!.absolutePath)
        } else{
            Gdx.files.internal("fonts/RopaSans-Regular.ttf")
        }
	}

	private fun getFontSize(fontSize: Float?): Int {
        return if (fontSize == null) {
            90
        } else {
            (fontSize * screenHeight).toInt()
        }
	}

	private fun initRoot() {
		root = Group().apply {
			width = screenWidth
			height = screenHeight
			x = 0f
			y = 0f
		}
		stage.addActor(root)
	}

	private fun initLogoSmall() {
		logo = Image(Texture("images/logo-small.png")).apply {
			x = screenWidth
			y = (height / 2) - 5f
			addAction(Actions.moveTo(screenWidth - width - 15f, y, 0.5f, interpolation))
		}
		root.addActor(logo)
	}

	override fun hide() {
	}

	override fun render(delta: Float) {
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)
		stage.act(Math.min(Gdx.graphics.deltaTime, 1 / 30f))
		stage.draw()
		inputController.update(delta)
	}

	override fun pause() {
	}

	override fun resume() {
	}

	override fun resize(width: Int, height: Int) {
	}

    override fun release() {
        inputController.dispose()
    }

    private fun launchGame() {

        if (listView.selectedIndex == -1) {
            error("No game was selected")
        }


        val game = selectedGame!!

        logger.info { "launchGame: ${game.path.name}" }

        val command = platform.runCommand.map {
            when {
                it.contains("%ROM_RAW%") ->
                    it.replace("%ROM_RAW%", game.path.absolutePath)
                it.contains("%ROM%") ->
                    it.replace("%ROM%", game.path.absolutePath) //TODO check emulationstation documentation
                it.contains("%BASENAME%") ->
                    it.replace("%BASENAME%", game.path.nameWithoutExtension)
                else -> it
            }
        }

        emulio.options.minimizeApplication()
        try {
            ProcessLauncher.executeProcess(command.toTypedArray())
        } catch (ex: ProcessException) {
            showErrorDialog("There was a problem launching this game. Check your config".translate())
        }

        emulio.options.restoreApplication()
    }

    private fun selectNext(amount: Int = 1) {
        val nextIndex = listView.selectedIndex + amount

        if (amount < 0) {
            if (nextIndex < 0) {
                listView.selectedIndex = listView.items.size + amount
            } else {
                listView.selectedIndex = nextIndex
            }
        }

        if (amount > 0) {
            if (nextIndex >= listView.items.size) {
                listView.selectedIndex = 0
            } else {
                listView.selectedIndex = nextIndex
            }
        }

        if (!isSelectionListView) {
            val list = if (needSelectionView) {
                filteredGames
            } else {
                games
            }

            selectedGame = list[listView.selectedIndex]
            updateGameSelected()
        }

        checkVisible(nextIndex)
    }

    private fun updateGameSelected() {
        lastTimer?.cancel()
        lastSequenceAction?.reset()

        logger.debug { if (selectedGame != null) { "updateGameSelected [${selectedGame!!.name}] [${selectedGame!!.path.name}]" } else { "no game" } }

        if (isBasicViewOnly()) {
            return
        }

        if (selectedGame == null) {
            clearDetailedView()
            return
        }


        val game = selectedGame!!

        val hasImage = game.image != null && game.image.isFile
        val texture = if (hasImage) {
            Texture(FileHandle(game.image), true)
        } else {
            Texture(0, 0, Pixmap.Format.RGB888)
        }

        texture.setFilter(Texture.TextureFilter.MipMap, Texture.TextureFilter.MipMap)
        gameImage.drawable = TextureRegionDrawable(TextureRegion(texture))
        gameImage.isVisible = hasImage

        gameReleaseDate.text = safeValue(if (game.releaseDate != null) {
            DateHelper.format(game.releaseDate)
        } else {
            null
        })

        gameDeveloper.text = safeValue(game.developer)
        gamePlayCount.text = "0"
        gameLastPlayed.text = "Never"
        gamePlayers.text = safeValue(game.players)
        gameGenre.text = safeValue(game.genre)
        gamePublisher.text = safeValue(game.publisher)
        gameDeveloper.text = safeValue(game.developer)

        descriptionScrollPane.scrollY = 0f
        gameDescription.setText(safeValue(game.description, ""))

        val gameRating = game.rating
        if (gameRating != null) {
            val rating = game.rating
            ratingImages.apply {
                ratingImg1.drawable = if (rating > 0.05) {
                    TextureRegionDrawable(TextureRegion(ratingFilledTexture))
                } else {
                    TextureRegionDrawable(TextureRegion(ratingUnFilledTexture))
                }
                ratingImg2.drawable = if (rating > 0.25) {
                    TextureRegionDrawable(TextureRegion(ratingFilledTexture))
                } else {
                    TextureRegionDrawable(TextureRegion(ratingUnFilledTexture))
                }
                ratingImg3.drawable = if (rating > 0.45) {
                    TextureRegionDrawable(TextureRegion(ratingFilledTexture))
                } else {
                    TextureRegionDrawable(TextureRegion(ratingUnFilledTexture))
                }
                ratingImg4.drawable = if (rating > 0.65) {
                    TextureRegionDrawable(TextureRegion(ratingFilledTexture))
                } else {
                    TextureRegionDrawable(TextureRegion(ratingUnFilledTexture))
                }
                ratingImg5.drawable = if (rating > 0.90) {
                    TextureRegionDrawable(TextureRegion(ratingFilledTexture))
                } else {
                    TextureRegionDrawable(TextureRegion(ratingUnFilledTexture))
                }

                ratingImg1.color = ratingColor
                ratingImg2.color = ratingColor
                ratingImg3.color = ratingColor
                ratingImg4.color = ratingColor
                ratingImg5.color = ratingColor
            }

        } else {

            ratingImages.apply {
                ratingImg1.drawable = TextureRegionDrawable(TextureRegion(ratingUnFilledTexture))
                ratingImg2.drawable = TextureRegionDrawable(TextureRegion(ratingUnFilledTexture))
                ratingImg3.drawable = TextureRegionDrawable(TextureRegion(ratingUnFilledTexture))
                ratingImg4.drawable = TextureRegionDrawable(TextureRegion(ratingUnFilledTexture))
                ratingImg5.drawable = TextureRegionDrawable(TextureRegion(ratingUnFilledTexture))

                ratingImg1.color = ratingColor
                ratingImg2.color = ratingColor
                ratingImg3.color = ratingColor
                ratingImg4.color = ratingColor
                ratingImg5.color = ratingColor
            }
        }

        animateDescription()

    }

    private fun clearDetailedView() {
        gameImage.isVisible = false
        ratingImages.apply {
            ratingImg1.color = Color.CLEAR
            ratingImg2.color = Color.CLEAR
            ratingImg3.color = Color.CLEAR
            ratingImg4.color = Color.CLEAR
            ratingImg5.color = Color.CLEAR
        }
        gameDeveloper.text = ""
        gamePlayCount.text = ""
        gameLastPlayed.text = ""
        gamePlayers.text = ""
        gameGenre.text = ""
        gamePublisher.text = ""
        gameDeveloper.text = ""
        gameReleaseDate.text = ""

        gameDescription.setText("")
    }

    private fun animateDescription() {
        lastTimer = Timer.schedule(object : Timer.Task() {


            override fun run() {

                if (gameDescription.height <= descriptionScrollPane.height) {
                    return
                }

                val scrollAmount = gameDescription.height - descriptionScrollPane.height
                val actionTime = scrollAmount * 0.05f


                val sequenceAction = SequenceAction(
                        ScrollByAction(0f, scrollAmount, actionTime),
                        Actions.delay(2f),
                        ScrollByAction(0f, -scrollAmount, actionTime)
                )

                lastSequenceAction = sequenceAction
                descriptionScrollPane.addAction(sequenceAction)
            }
        }, 2.5f)
    }

    private fun safeValue(string: String?, defaultText: String = "Unknown"): String {
        return string?.trim() ?: defaultText
    }

    private fun checkVisible(index: Int) {
        val itemHeight = listView.itemHeight

        val selectionY = index * itemHeight
        val selectionY2 = selectionY + itemHeight

        val minItemsVisible = itemHeight * 5

        val itemsPerView = listScrollPane.height / itemHeight

        if (isSelectionListView) {
            if (listView.selectedIndex > (27 - itemsPerView)) {
                listScrollPane.scrollY = listView.height - listScrollPane.height
                return
            }
        } else {
            val gamesList = if (needSelectionView) {
                filteredGames
            } else {
                games
            }

            if (listView.selectedIndex > (gamesList.size - itemsPerView)) {
                listScrollPane.scrollY = listView.height - listScrollPane.height
                return
            }
        }

        if (listView.selectedIndex == 0) {
            listScrollPane.scrollY = 0f
            return
        }

        if ((selectionY2 + minItemsVisible) > listScrollPane.height) {
            listScrollPane.scrollY = (selectionY2 - listScrollPane.height) + minItemsVisible
        }

        val minScrollY = Math.max(selectionY - minItemsVisible, 0f)

        if (minScrollY < listScrollPane.scrollY) {
            listScrollPane.scrollY = minScrollY
        }
    }

    private fun buildHelpHuds(initialY: Float, height: Float) {

        val calculatedHeight = height * 0.55f

        val helpFont = freeTypeFontGenerator.generateFont(FreeTypeFontGenerator.FreeTypeFontParameter().apply {
            size = calculatedHeight.toInt()
            color = Color.WHITE
            color.a = 1f
        })

        // Calculate the size according resolution???
        val imgWidth = calculatedHeight
        val imgHeight = calculatedHeight
        val padding = 5f

        val lineHeight = helpFont.lineHeight

        val y = initialY - 2f + ((height - lineHeight) / 2)
        val imageY = (initialY - 2f + ((height - imgHeight) / 2)) + 2f

        val imgOptions = buildImage("images/resources/help/button_select_128_128.png", imgWidth, imgHeight, 10f, imageY)
        stage.addActor(imgOptions)
        val txtOptions = buildText("Options".translate().toUpperCase(), helpFont, imgOptions.x + imgWidth + padding, y)
        stage.addActor(txtOptions)

        val imgStart = buildImage("images/resources/help/button_start_128_128.png", imgWidth, imgHeight, txtOptions.x + txtOptions.width + (padding * 3), imageY)
        stage.addActor(imgStart)
        val txtMenu = buildText("Menu".translate().toUpperCase(), helpFont, imgStart.x + imgWidth + padding, y)
        stage.addActor(txtMenu)

        val imgB = buildImage("images/resources/help/button_b_128_128.png", imgWidth, imgHeight, txtMenu.x + txtMenu.width + (padding * 3), imageY)
        stage.addActor(imgB)
        val txtBack = buildText("Back".translate().toUpperCase(), helpFont, imgB.x + imgWidth + padding, y)
        stage.addActor(txtBack)

        val imgA = buildImage("images/resources/help/button_a_128_128.png", imgWidth, imgHeight, txtBack.x + txtBack.width + (padding * 3), imageY)
        stage.addActor(imgA)
        val txtSelect = buildText("Launch".translate().toUpperCase(), helpFont, imgA.x + imgWidth + padding, y)
        stage.addActor(txtSelect)

        val imgDPadUpDown = buildImage("images/resources/help/dpad_updown_128_128.png", imgWidth, imgHeight, txtSelect.x + txtSelect.width + (padding * 3), imageY)
        stage.addActor(imgDPadUpDown)
        val txtSystem = buildText("System".translate().toUpperCase(), helpFont, imgDPadUpDown.x + imgWidth + padding, y)
        stage.addActor(txtSystem)

        val imgDPadLeftRight = buildImage("images/resources/help/dpad_leftright_128_128.png", imgWidth, imgHeight, txtSystem.x + txtSystem.width + (padding * 3), imageY)
        stage.addActor(imgDPadLeftRight)
        val txtChoose = buildText("Choose".translate().toUpperCase(), helpFont, imgDPadLeftRight.x + imgWidth + padding, y)
        stage.addActor(txtChoose)

        val alpha = 0.4f
        val imgColor = Color.BLACK
        val txtColor = Color.BLACK

        imgOptions.color = imgColor
        imgOptions.color.a = alpha
        txtOptions.color = txtColor
        txtOptions.color.a = alpha
        imgStart.color = imgColor
        imgStart.color.a = alpha
        txtMenu.color = txtColor
        txtMenu.color.a = alpha
        imgB.color = imgColor
        imgB.color.a = alpha
        txtBack.color = txtColor
        txtBack.color.a = alpha
        imgA.color = imgColor
        imgA.color.a = alpha
        txtSelect.color = txtColor
        txtSelect.color.a = alpha
        imgDPadUpDown.color = imgColor
        imgDPadUpDown.color.a = alpha
        txtSystem.color = txtColor
        txtSystem.color.a = alpha
        imgDPadLeftRight.color = imgColor
        imgDPadLeftRight.color.a = alpha
        txtChoose.color = txtColor
        txtChoose.color.a = alpha

    }

    private fun buildText(text: String, txtFont: BitmapFont, x: Float, y: Float): Label {
        return Label(text, Label.LabelStyle().apply {
            font = txtFont
        }).apply {
            setPosition(x, y)
            color = Color.WHITE
        }
    }

    private fun buildImage(imgPath: String, imgWidth: Float, imgHeight: Float, x: Float, y: Float): Image {
        return buildImage(buildTexture(imgPath), imgWidth, imgHeight, x, y)
    }

    private fun buildImage(texture: Texture, imgWidth: Float, imgHeight: Float, x: Float, y: Float): Image {
        val imgButtonStart = Image(texture)
        imgButtonStart.setSize(imgWidth, imgHeight)
        imgButtonStart.x = x
        imgButtonStart.y = y
        return imgButtonStart
    }

    private fun buildTexture(imgPath: String): Texture {
        return Texture(Gdx.files.internal(imgPath), true).apply {
            setFilter(Texture.TextureFilter.MipMap, Texture.TextureFilter.MipMap)
        }
    }

    override fun onConfirmButton(): Boolean {
        if (isSelectionListView) {
            filteredGames = when(listView.selectedIndex) {
                0 -> games.filter { it.displayName!![0].isDigit() }
                1 -> games.filter { it.displayName!!.toUpperCase().startsWith("A") }
                2 -> games.filter { it.displayName!!.toUpperCase().startsWith("B") }
                3 -> games.filter { it.displayName!!.toUpperCase().startsWith("C") }
                4 -> games.filter { it.displayName!!.toUpperCase().startsWith("D") }
                5 -> games.filter { it.displayName!!.toUpperCase().startsWith("E") }
                6 -> games.filter { it.displayName!!.toUpperCase().startsWith("F") }
                7 -> games.filter { it.displayName!!.toUpperCase().startsWith("G") }
                8 -> games.filter { it.displayName!!.toUpperCase().startsWith("H") }
                9 -> games.filter { it.displayName!!.toUpperCase().startsWith("I") }
                10 -> games.filter { it.displayName!!.toUpperCase().startsWith("J") }
                11 -> games.filter { it.displayName!!.toUpperCase().startsWith("K") }
                12 -> games.filter { it.displayName!!.toUpperCase().startsWith("L") }
                13 -> games.filter { it.displayName!!.toUpperCase().startsWith("M") }
                14 -> games.filter { it.displayName!!.toUpperCase().startsWith("N") }
                15 -> games.filter { it.displayName!!.toUpperCase().startsWith("O") }
                16 -> games.filter { it.displayName!!.toUpperCase().startsWith("P") }
                17 -> games.filter { it.displayName!!.toUpperCase().startsWith("Q") }
                18 -> games.filter { it.displayName!!.toUpperCase().startsWith("R") }
                19 -> games.filter { it.displayName!!.toUpperCase().startsWith("S") }
                20 -> games.filter { it.displayName!!.toUpperCase().startsWith("T") }
                21 -> games.filter { it.displayName!!.toUpperCase().startsWith("U") }
                22 -> games.filter { it.displayName!!.toUpperCase().startsWith("V") }
                23 -> games.filter { it.displayName!!.toUpperCase().startsWith("W") }
                24 -> games.filter { it.displayName!!.toUpperCase().startsWith("X") }
                25 -> games.filter { it.displayName!!.toUpperCase().startsWith("Y") }
                26 -> games.filter { it.displayName!!.toUpperCase().startsWith("Z") }
                else -> games
            }

            listView.remove()
            listScrollPane.remove()
            buildListScrollPane({ buildGameListView(gamelistView, filteredGames) })
            listView.selectedIndex = 0
            selectedGame = filteredGames[0]
            updateGameSelected()
        } else {
            launchGame()
        }
        return true
    }


    override fun onCancelButton(): Boolean {
        if (!guiready) return false

        if (needSelectionView && !isSelectionListView) {
            listView.remove()
            listScrollPane.remove()
            buildListScrollPane({ buildSelectionListView()})
            listView.selectedIndex = 0
            selectedGame = null
            updateGameSelected()
        } else {
            switchScreen(PlatformsScreen(emulio, platform))
        }

        return true
    }

    override fun onUpButton(): Boolean {
        if (!guiready) return false

        selectNext(-1)
        return true
    }

    override fun onDownButton(): Boolean {
        logger.debug { "onDownButton ${System.identityHashCode(this)} ${platform.platformName} $guiready" }
        if (!guiready) return false

        selectNext()
		return true
	}


    override fun onLeftButton(): Boolean {
        logger.debug { "onLeftButton ${System.identityHashCode(this)} ${platform.platformName} $guiready" }
        if (!guiready) return false

        val platforms = emulio.platforms
        val index = platforms.indexOf(platform)

        val previousPlatform = if (index > 0) {
            index - 1
        } else {
            platforms.size - 1
        }

        switchScreen(GameListScreen(emulio, platforms[previousPlatform]))
		return true
	}

	override fun onRightButton(): Boolean {
        logger.debug { "onRightButton ${System.identityHashCode(this)} ${platform.platformName} $guiready" }
        if (!guiready) return false

        val platforms = emulio.platforms
        val index = platforms.indexOf(platform)

        val previousPlatform = if (index > platforms.size - 2) {
            0
        } else {
            index + 1
        }
        switchScreen(GameListScreen(emulio, platforms[previousPlatform]))
		return true
	}

	override fun onFindButton(): Boolean {
        logger.debug { "onFindButton ${System.identityHashCode(this)} ${platform.platformName} $guiready" }

        if (!guiready) return false
		return true
	}
    
	override fun onOptionsButton(): Boolean {
        showMainMenu()
		return true
	}

	override fun onSelectButton(): Boolean {
        logger.debug { "onSelectButton ${System.identityHashCode(this)} ${platform.platformName} $guiready" }
        if (!guiready) return false
		return true
	}

	override fun onPageUpButton(): Boolean {
        logger.debug { "onPageUpButton ${System.identityHashCode(this)} ${platform.platformName} $guiready" }
        if (!guiready) return false
        selectNext(-10)
        return true
	}

	override fun onPageDownButton(): Boolean {
        logger.debug { "onPageDownButton ${System.identityHashCode(this)} ${platform.platformName} $guiready" }
        if (!guiready) return false
        selectNext(10)
		return true
	}

	override fun onExitButton(): Boolean {
        logger.debug { "onExitButton ${System.identityHashCode(this)} ${platform.platformName} $guiready" }
        if (!guiready) return false

        showCloseDialog()

		return true
	}


}

class ScrollByAction(private val endScrollX: Float, private val endScrollY: Float, duration: Float) : TemporalAction(duration) {

    private lateinit var scrollPane: ScrollPane

    private var startScrollX: Float = -1f
    private var startScrollY: Float = -1f

    override fun begin() {
        scrollPane = target as ScrollPane
        startScrollX = scrollPane.scrollX
        startScrollY = scrollPane.scrollY
    }

    override fun update(percent: Float) {
        scrollPane.scrollX = startScrollX + (endScrollX - startScrollX) * percent
        scrollPane.scrollY = startScrollY + (endScrollY - startScrollY) * percent
    }
}