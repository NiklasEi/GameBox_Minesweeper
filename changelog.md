### v 3.1.0
- compatibility with minecraft 1.13

### v 3.0.0
- compatible with gamebox v2
- fuse competition and 'hard' game-modes

# 

### v2.3.1
- check inventory title length if GB flag is set
- removed leftover 1.8 checks in language from the old 1.x versions


### v 2.3.0
- corrected lang en
- push GB dep to 1.5.0
  - now compatible with /gba reload


### v 2.2.0
- centralised more code to GameBox
  - use static main-key from GUIManager (GameBox) for guis
  - chat color in Main class
  - Sounds
  - use ItemStackUtil from GameBox to load ItemStacks
- removed deprecated methods and variables (now depends on GB version 1.3.0)
- add spanish file
- add mandarin file
- mkdir for lang folder...
- defaults all on small grid!
- replace all non numbers in version

### v 2.1.1:
- 'big-mode'
- changed nodes 'token' to 'tokens' in config to fix not any tokens being given to players (internally 'tokens' is used)
  - game reads both for backward compatibility
- mines in default hard from 11 to 10
- accepting 'default' and 'default.yml' as lang file and changed default value in config to 'default'

### v 1.8.1:
- corrected top list head for /mstop e:n:h