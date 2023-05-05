# xxSherly

A fork of [Sherly](https://github.com/BlyDoesCoding/Sherly), using [xxHash](https://github.com/Cyan4973/xxHash).

## Introduction

Sherly is a Multithreaded Duplicate File Finder for your Terminal, written in java. You can Easily find duplicate Images, videos as well as any other type of Data. That can be helpful if you run on small storage or just want to keep regular housekeeping.

## Full Usages

```console
Usage: sherly -f inputfolder1 inputfolder2 inputfolder3 [options]...
 
   -h / -help             show this
   -f / -folder           all the folders you want to scan for (see example above!)
   -c / -color            enable colored messages
   -t / -threads          override default Thread number (default is usually number of cores * 2)
   -p / -progress         enable progress indicator
   -d / -delete           delete all dups except one without asking first
   -n / -noinput          skip all user input
   -debug                 debug stuff
```

## Build

```bash
mvn package assembly:single
```

## Supported Platforms

| OS                |    Working     | Version |
| :----:            |:--------------:|:-------:|
| Linux             |      Yes       |  1.0    |
| Windows 10/11/8/7 | Not yet tested | |
| BSD               | Not yet tested | |

## Screenshots

![screenshot](https://github.com/BlyDoesCoding/Sherly/blob/master/Images/screenshot?raw=true)
