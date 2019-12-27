package com.github.emulio.ui.screens

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.files.FileHandle
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.Pixmap
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.math.Interpolation
import com.badlogic.gdx.scenes.scene2d.Actor
import com.badlogic.gdx.scenes.scene2d.Group
import com.badlogic.gdx.scenes.scene2d.InputEvent
import com.badlogic.gdx.scenes.scene2d.actions.Actions
import com.badlogic.gdx.scenes.scene2d.actions.SequenceAction
import com.badlogic.gdx.scenes.scene2d.actions.TemporalAction
import com.badlogic.gdx.scenes.scene2d.ui.*
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable
import com.badlogic.gdx.utils.Align
import com.badlogic.gdx.utils.Scaling
import com.badlogic.gdx.utils.Timer
import com.github.emulio.Emulio
import com.github.emulio.model.AnyInputConfig
import com.github.emulio.model.Game
import com.github.emulio.model.InputConfig
import com.github.emulio.model.Platform
import com.github.emulio.model.theme.*
import com.github.emulio.process.ProcessException
import com.github.emulio.process.ProcessLauncher
import com.github.emulio.ui.input.InputListener
import com.github.emulio.ui.input.InputManager
import com.github.emulio.ui.screens.dialogs.InfoDialog
import com.github.emulio.ui.screens.keyboard.VirtualKeyboardDialog
import com.github.emulio.utils.DateHelper
import com.github.emulio.utils.translate
import mu.KotlinLogging
import java.io.File


class GameListScreen(
        emulio: Emulio,
        val platform: Platform) : EmulioScreen(emulio), InputListener {

    val logger = KotlinLogging.logger { }

	private val inputController: InputManager = InputManager(this, emulio, stage)
	private val interpolation = Interpolation.fade

    private lateinit var games: List<Game>

    private var selectedGame: Game? = null

    private lateinit var listView: com.badlogic.gdx.scenes.scene2d.ui.List<String>
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
    private lateinit var ratingImages: RatingImages

    private var lastTimer: Timer.Task? = null
    private var lastSequenceAction: SequenceAction? = null

    init {
		Gdx.input.inputProcessor = inputController

        prepareGamesList(emulio)
        initGUI()
    }


    private fun prepareGamesList(emulio: Emulio, gamesFound: List<Game> = filterGames(emulio)) {

        val supportedExtensions = platform.romsExtensions
        val gamesMap = mutableMapOf<String, Game>()

        gamesFound.forEach { game ->

            val nameWithoutExtension = game.path.nameWithoutExtension

            val gameFound: Game? = gamesMap[nameWithoutExtension]
            if (gameFound != null) {
                if (gameFound.path.name != nameWithoutExtension) {
                    gamesMap[nameWithoutExtension] = game
                    game.displayName = nameWithoutExtension

                } else if (gameFound.path.name == nameWithoutExtension) {

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

        games = gamesMap.values.sortedBy {
            it.displayName!!.toLowerCase()
        }
    }

    private fun filterGames(emulio: Emulio, customFilter: ((Game) -> Boolean)? = null): List<Game> {
        return if (customFilter == null) {
            emulio.games!![platform]?.toList() ?: emptyList()
        } else {
            (emulio.games!![platform]?.toList() ?: emptyList()).filter(customFilter)
        }
    }

    private fun isBasicViewOnly(): Boolean {
        return games.none { it.id != null || it.description != null || it.image != null }
    }

    private var guiReady: Boolean = false

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
        guiReady = true

        if (games.size > 1) {
            listView.selectedIndex = 0
            selectedGame = games[0]
            updateGameSelected()
        }
    }

    private fun buildBasicView(basicView: View) {
		gamelistView = basicView.findViewItem("gamelist") as TextList
        buildListScrollPane { buildListView() }
	}

    private var lastSelectedIndex: Int = -1

    private fun buildListScrollPane(builder: () -> com.badlogic.gdx.scenes.scene2d.ui.List<String>) {
        listView = builder()

        listView.addListener(object : ClickListener() {
            override fun clicked(event: InputEvent?, x: Float, y: Float) {
                val newIndex = listView.selectedIndex

                if (lastSelectedIndex == newIndex) {
                    onConfirmButton(AnyInputConfig)
                    return
                }

                selectedGame = games[newIndex]
                updateGameSelected()
                lastSelectedIndex = listView.selectedIndex
            }
        })

        listScrollPane = ScrollPane(listView, ScrollPane.ScrollPaneStyle()).apply {
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
            val platformImage = buildImage(logo, Scaling.fit)

            stage.addActor(platformImage)
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

        initHelpHuds(footerY, footerHeight, HelpItems(
            txtSelect = "Options".translate().toUpperCase(),
            txtOptions = "Menu".translate().toUpperCase(),
            txtCancel = "Back".translate().toUpperCase(),
            txtConfirm = "Launch".translate().toUpperCase(),
            txtLeftRight = "System".translate().toUpperCase(),
            txtUpDown = "Choose".translate().toUpperCase(),

            alpha = 0.9f,
            txtColor = Color(0x666666FF)
        ))
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

            setAlign(Align.left)

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
			alignment = when(textView.alignment) {
                TextAlignment.LEFT -> Align.left
                TextAlignment.RIGHT -> Align.right
                TextAlignment.CENTER -> Align.center
                TextAlignment.JUSTIFY -> Align.left //TODO
            }
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

	private fun buildListView(): com.badlogic.gdx.scenes.scene2d.ui.List<String> {
        return buildGameListView(gamelistView, games)
	}

    private fun buildGameListView(gamelistView: TextList, selectedGames: List<Game>): com.badlogic.gdx.scenes.scene2d.ui.List<String> {

        return List<String>(com.badlogic.gdx.scenes.scene2d.ui.List.ListStyle().apply {
            fontColorUnselected = getColor(gamelistView.primaryColor)
            fontColorSelected = getColor(gamelistView.selectedColor)
            font = getFont(getFontPath(gamelistView), getFontSize(gamelistView.fontSize))

            val selectorTexture = createColorTexture(Integer.parseInt(gamelistView.selectorColor + "FF", 16))
            selection = TextureRegionDrawable(TextureRegion(selectorTexture))

        }).apply {
            setSize(gamelistView)

            selectedGames.forEach { game ->
                items.add(game.displayName)
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
            width = width.coerceAtLeast(screenWidth * viewItem.maxSizeX!!)
        }
        if (viewItem.maxSizeY != null) {
            height = height.coerceAtLeast(screenHeight * viewItem.maxSizeY!!)
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
			width = width.coerceAtMost(screenWidth * viewItem.maxSizeX!!)
		}
		if (viewItem.maxSizeY != null) {
			height = height.coerceAtMost(screenHeight * viewItem.maxSizeY!!)
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
		stage.act(Gdx.graphics.deltaTime.coerceAtMost(1 / 30f))
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

        val list = games
        selectedGame = list[listView.selectedIndex]
        updateGameSelected()

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

        val gamesList = games
        if (listView.selectedIndex > (gamesList.size - itemsPerView)) {
            listScrollPane.scrollY = listView.height - listScrollPane.height
            return
        }

        if (listView.selectedIndex == 0) {
            listScrollPane.scrollY = 0f
            return
        }

        if ((selectionY2 + minItemsVisible) > listScrollPane.height) {
            listScrollPane.scrollY = (selectionY2 - listScrollPane.height) + minItemsVisible
        }

        val minScrollY = (selectionY - minItemsVisible).coerceAtLeast(0f)

        if (minScrollY < listScrollPane.scrollY) {
            listScrollPane.scrollY = minScrollY
        }
    }

    override fun buildImage(imgPath: String, imgWidth: Float, imgHeight: Float, x: Float, y: Float): Image {
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

    override fun onConfirmButton(input: InputConfig) {
        updateHelp()
        if (!guiReady) return

        launchGame()
    }


    override fun onCancelButton(input: InputConfig) {
        updateHelp()
        if (!guiReady) return

        switchScreen(PlatformsScreen(emulio, platform))
    }

    override fun onUpButton(input: InputConfig) {
        updateHelp()
        if (!guiReady) return

        selectNext(-1)
    }

    override fun onDownButton(input: InputConfig) {
        updateHelp()
        logger.debug { "onDownButton ${System.identityHashCode(this)} ${platform.platformName} $guiReady" }
        if (!guiReady) return

        selectNext()
	}


    override fun onLeftButton(input: InputConfig) {
        updateHelp()
        logger.debug { "onLeftButton ${System.identityHashCode(this)} ${platform.platformName} $guiReady" }
        if (!guiReady) return

        val platforms = emulio.platforms
        val index = platforms.indexOf(platform)

        val previousPlatform = if (index > 0) {
            index - 1
        } else {
            platforms.size - 1
        }

        switchScreen(GameListScreen(emulio, platforms[previousPlatform]))
	}

	override fun onRightButton(input: InputConfig) {
        updateHelp()
        logger.debug { "onRightButton ${System.identityHashCode(this)} ${platform.platformName} $guiReady" }
        if (!guiReady) return

        val platforms = emulio.platforms
        val index = platforms.indexOf(platform)

        val previousPlatform = if (index > platforms.size - 2) {
            0
        } else {
            index + 1
        }
        switchScreen(GameListScreen(emulio, platforms[previousPlatform]))
	}

	override fun onFindButton(input: InputConfig) {
        updateHelp()
        logger.debug { "onFindButton ${System.identityHashCode(this)} ${platform.platformName} $guiReady" }
        if (!guiReady) return

        VirtualKeyboardDialog("Search", "Message", emulio, stage) { text ->
            handleSearch(filterByText(text))
        }.show(stage)


	}
    
	override fun onOptionsButton(input: InputConfig) {
        updateHelp()

        Gdx.app.postRunnable {
            showMainMenu {
                GameListScreen(emulio, platform)
            }
        }
	}

	override fun onSelectButton(input: InputConfig) {
        updateHelp()
        logger.debug { "onSelectButton ${System.identityHashCode(this)} ${platform.platformName} $guiReady" }
        if (!guiReady) return

        Gdx.app.postRunnable {
            showOptionsMenu { response ->
                val searchDialogText = response.searchDialogText
                val jumpToLetter = response.jumpToLetter

                if (searchDialogText != null) {
                    handleSearch(filterByText(searchDialogText))
                } else if (jumpToLetter != null) {
                    handleSearch(filterByLetter(jumpToLetter))
                }
            }
        }
	}

    private fun filterByLetter(jumpToLetter: Char): (Game) -> Boolean {
        return { game ->
            val displayName = game.displayName
            val name = game.name

            val containsName = name?.toLowerCase()?.startsWith(jumpToLetter.toLowerCase()) ?: false
            val containsDisplayName = displayName?.toLowerCase()?.startsWith(jumpToLetter.toLowerCase()) ?: false

            containsName || containsDisplayName
        }
    }

    private fun filterByText(searchDialogText: String): (Game) -> Boolean {
        return { game ->
            val displayName = game.displayName
            val name = game.name

            val containsName = name?.toLowerCase()?.contains(searchDialogText) ?: false
            val containsDisplayName = displayName?.toLowerCase()?.contains(searchDialogText) ?: false

            containsName || containsDisplayName
        }
    }

    private fun handleSearch(customFilter: ((Game) -> Boolean)?) {

        val gamesFound = filterGames(emulio, customFilter)

        if (gamesFound.isNotEmpty()) {
            stage.actors.clear()
            prepareGamesList(emulio, gamesFound)
            initGUI()
            selectNext(1)
        } else {
            Gdx.app.postRunnable {
                InfoDialog(
                        "No games found".translate(),
                        "No games found, please change your criteria.".translate(),
                        emulio).show(stage)
            }
        }
    }

    override fun onPageUpButton(input: InputConfig) {
        updateHelp()
        logger.debug { "onPageUpButton ${System.identityHashCode(this)} ${platform.platformName} $guiReady" }
        if (!guiReady) return
        selectNext(-10)
	}

	override fun onPageDownButton(input: InputConfig) {
        updateHelp()
        logger.debug { "onPageDownButton ${System.identityHashCode(this)} ${platform.platformName} $guiReady" }
        if (!guiReady) return
        selectNext(10)
	}

	override fun onExitButton(input: InputConfig) {
        updateHelp()
        if (!guiReady) return

        showCloseDialog()

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