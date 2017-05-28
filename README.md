
![logo](https://s1.postimg.org/mrtbd0yhr/emulio.png)

# emulio
Emulators frontend inspired by EmulationStation but written in a better platform to support better usability and eye candy.

## Backlog/Current status
- [x] Import all things from emulationstation data, try to be compatible, or have an iport option. - WIP
  - [x] Themes
  - [x] Gamelist 
  - [x] Platforms
- [x] Import gamelists from generic scrappers
- [ ] Have a query mechanism, by publisher, by name, custom finders
- [x] Reduce startup latency, avoid discovery in start and run in background
- [ ] Improve deep searches/updates performance - TODO
- [ ] Themes from emulationstation support. - WIP
- [ ] Store all data, information, settings in a much more faster format
- [ ] Configurations, settings, another things are stored in yaml format to better compreehension and better maintenance 
- [ ] Always be a portable mode - WIP
- [ ] Settings must be done/configured all in GUI and not only in yaml files. - TODO
- [ ] libGDX based
  - [ ] Scene2D items
  - [x] Splash Screen
  - [ ] OnScreen menus
  - [ ] Imported theme views - WIP
  - [ ] Options Screen
  - [ ] Multiresolution/Fullscreen
  - [ ] Annimations
  - [ ] Keyboard Input
  - [ ] Gamepad Input
  - [x] Multithreading messages (see rxjava)
- [x] Launch external process and control it
- [x] RxJava to control threads/subscribers
- [x] Model data structures
- [ ] Add MongoDB as a faster storage
- [x] Fastest mechanism to detect folder changes - nio.Files is being used
  
