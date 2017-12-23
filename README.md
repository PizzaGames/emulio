
![logo](https://s1.postimg.org/mrtbd0yhr/emulio.png)

[![Build Status](https://travis-ci.org/PizzaGames/emulio.svg?branch=master)](https://travis-ci.org/PizzaGames/emulio)
[![Waffle.io - Columns and their card count](https://badge.waffle.io/PizzaGames/emulio.svg?columns=Backlog,To%20do,In%20Progress)](https://waffle.io/PizzaGames/emulio)
[![codebeat](https://codebeat.co/badges/7fc9e22e-807e-4742-b129-d6062397beef)](https://codebeat.co/projects/github-com-pizzagames-emulio-master)
[![License: GPL v3](https://img.shields.io/badge/License-GPL%20v3-blue.svg)](https://www.gnu.org/licenses/gpl-3.0)

# emulio
Emulators frontend inspired by EmulationStation but written in a better platform to support better usability and eye candy.

This reimplementation of the same as emulationstation is done to achieve a much better performance in the usability and a much better way to configure all the settings.

EmulationStation project is great but the PC version is abandonned and since is written in C++ the maintenance can be cruel/brutal. So I decided to create the emulio project. That is written in a much easier programming language and using modern components such as libgdx and kotlin to have a better maintenance and possibly future developers to help me in the development

Initially this is focused on Windows, Linux and MAC platforms, but in the long term the idea is to be totally crossplatform. LibGDX are going to be the limit, that means, what LibGDX supports, we will try to keep this supported as well

## Documentation
You can check our wiki pages. It consists in two parts. One for Users and other for developers. Check out our wiki [here](https://github.com/PizzaGames/emulio/wiki)
- [Wiki](https://github.com/PizzaGames/emulio/wiki)

## Community
Everyone is welcome to help the project to evolve. The main idea is to have a more crossplatform emulationstation alternative to every enviroment and with a great performance.

## Performance
The EmulationStation has some performance problems and in some enviroments it can take too much time to even start the application. Emulio is here to solve this problem. All loadings are lazy and run on another threads with incremental updates, so user can start enjoy even if every files are still being read.

On an initial tests, EmulationStation was taking up to 5 minutes to load an full collection of games from several systems. The same ammount of information is read by emulio in 11 seconds.

This gain in performance made this project continue in a way to achieve a better support software than the original EmulationStation

## License 
Emulio is under GPLv3. When I (the initial developer) started this development I wanted emulio to be a free and open version to everyone and with the intention of always evolve and without anyone getting it's source and closing for another purpose.. The main idea here, is to prevent emulio to be more evolved but closed for a limited audience. The main reason is to get to everyone with its source for people contribute if they want.

## Backlog/Current status
You can see what's planned and in development in the waffle.io boards :)
- [https://waffle.io/PizzaGames/emulio](https://waffle.io/PizzaGames/emulio)
