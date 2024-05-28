# java-mp3-fuse
# Группировка mp3 файлов

У mp3 файлов есть теги содержащие разную мета информации об аудиофайле, например имя
исполнителя, год записи песни, жанр и т.д. Ваша программа будет получать путь до директории с mp3
файлами и должна будет пройдясь рекурсивно по всем поддиректориям составить 3 директории в
которых будут сгруппированы файлы согласно тегам: Artist, Year и Genre.

### Пример:

.
├── grouped_mp3
│ ├── Artist
│ │ ├── Gary Jules
│ │ │ └── mad_world-Gary_Jules.mp3
│ │ ├── Lofi Girl
│ │ │ └── lofi_beats_to_relax_study_to.mp3
│ │ ├── Mick Gordon
│ │ │ └── mick gordon RIP&TEAR.mp3
│ │ └── no_artist
│ │ └── Без названия - Неизвестен.mp3
│ ├── Genre
│ │ ├── Hip-Hop
│ │ │ └── lofi_beats_to_relax_study_to.mp3
│ │ ├── Metal
│ │ │ └── mick gordon RIP&TEAR.mp3
│ │ ├── New Wave
│ │ │ └── mad_world-Gary_Jules.mp3
│ │ ├── no_genre
│ │ │ └── Без названия - Неизвестен.mp3
│ │ ├── Soundtrack
│ │ │ └── mick gordon RIP&TEAR.mp3
│ │ └── Synthpop
│ │ └── mad_world-Gary_Jules.mp3
│ └── Year
│ ├── 1982
│ │ └── mad_world-Gary_Jules.mp3
│ ├── 2016
│ │ └── mick gordon RIP&TEAR.mp3
│ └── no_year
│ ├── lofi_beats_to_relax_study_to.mp3
│ └── Без названия - Неизвестен.mp3
└── original_dir
├── cool_vibes
│ └── lofi_beats_to_relax_study_to.mp3
├── mick gordon RIP&TEAR.mp3
└── музыка из вк
├── mad_world-Gary_Jules.mp3
└── Без названия - Неизвестен.mp3

- ФС должна быть read only
- Обратите внимание что у некоторых файлов может быть несколько жанров
- При попытке прочитать файл из примонтированной директории должен отдаваться оригинальный файл
