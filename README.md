
![logo](https://i.imgur.com/wiJroeX.png)

[![Build Status](https://travis-ci.org/PizzaGames/emulio.svg?branch=master)](https://travis-ci.org/PizzaGames/emulio)
[![codebeat badge](https://codebeat.co/badges/7130fcf8-b3b3-4325-8479-188336e1d4d0)](https://codebeat.co/projects/github-com-pizzagames-emulio-master)
[![License: GPL v3](https://img.shields.io/badge/License-GPL%20v3-blue.svg)](https://www.gnu.org/licenses/gpl-3.0)

# emulio
Emulators frontend inspired by EmulationStation but written in a better platform to support better usability and eye candy.

This reimplementation of the same as EmulationStation is done to achieve a much better performance in the usability and a much better way to configure all the settings.

EmulationStation project is great but the Windows/Linux version is abandoned and since is written in C++ the maintenance can be cruel/brutal. 

So I decided to create the emulio project. That is written in a much easier programming language and using modern components such as libgdx and kotlin to have a better maintenance and possibly future developers to help me in the development

Initially this is focused on Windows, Linux and MAC platforms, but in the long term the idea is to be totally cross-platform. LibGDX are going to be the limit, that means, what LibGDX supports, we will try to keep this supported as well

## Documentation
You can check our wiki pages. It consists in two parts. One for Users and other for developers. Check out our wiki [here](https://github.com/PizzaGames/emulio/wiki)
- [Wiki](https://github.com/PizzaGames/emulio/wiki)

## Community
Everyone is welcome to help the project to evolve. The main idea is to have a more cross-platform emulation station alternative to every environment and with a great performance.

## Download and Run

You can find all the latest releases on our releases page from github or clicking [here](https://github.com/PizzaGames/emulio/releases)

After you download you can simply run the executable file. In the first moments, only the jar and executable files are being available, but you can run in Linux/Mac environments just using Java, with the command:

```bash
javaw -jar emulio.jar
```

And if you want to check some logs, you can also use the command:

```bash
java -jar emulio.jar
```



## Performance
The EmulationStation has some performance problems and in some enviroments it can take too much time to even start the application. Emulio is here to solve this problem. All loadings are lazy and run on another threads with incremental updates, so user can start enjoy even if every files are still being read.

On an initial tests, EmulationStation was taking up to 5 minutes to load an full collection of games from several systems. The same ammount of information is read by emulio in 11 seconds.

This gain in performance made this project continue in a way to achieve a better support software than the original EmulationStation

## License 
Emulio is under GPLv3. When I (the initial developer) started this development I wanted emulio to be a free and open version to everyone and with the intention of always evolve and without anyone getting it's source and closing for another purpose.. The main idea here, is to prevent emulio to be more evolved but closed for a limited audience. The main reason is to get to everyone with its source for people contribute if they want.

## Development info

### Run and debug commands

To build the projects we are using gradle, like normal libgdx projects are using normally.

You can build the project with the command:

```bash
./gradlew build
```

To build and run emulio from command line:

```bash
./gradlew desktop:run
```



You can check more documentation in the libgdx official documentation page:

- https://libgdx.badlogicgames.com/documentation/gettingstarted/Running%20and%20Debugging.html



## Backlog/Current status

You can check our current backlog in the following URL:

- https://github.com/PizzaGames/emulio/projects/2

If you want to contribute, just let us know, but feel free to help us to make this project happen :)