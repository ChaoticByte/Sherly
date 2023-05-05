# xxSherly

A fork of [Sherly](https://github.com/BlyDoesCoding/Sherly), using [xxHash](https://github.com/Cyan4973/xxHash).

![](./images/screenshot.png)

## Introduction

Sherly is a Multithreaded Duplicate File Finder for your Terminal, written in java. You can Easily find duplicate Images, videos as well as any other type of Data. That can be helpful if you run on small storage or just want to keep regular housekeeping.

This fork uses [xxHash](https://github.com/Cyan4973/xxHash) instead of MD5 for performance reasons (see [Speed comparison](#speed-comparison)).
Note that xxHash is not a cryptographic hash function and therefore may produce collisions. That's why the checksum is composed of the xxHash Digest and the filesize.

## Usage

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
| ----------------- | :------------: | ------: |
| Linux             |      Yes       |     1.0 |
| Windows 10/11     | Not yet tested |       - |
| macOS             | Not yet tested |       - |
| BSD               | Not yet tested |       - |

## Speed comparison

I let Sherly and xxSherly find duplicates in my Music Library (containing `.wav` files) using the following commands:

```bash
time java -jar Bin/sherly.jar -n -f ~/Music/
time java -jar target/xxSherly-1.0-jar-with-dependencies.jar -n -f ~/Music/
```

The timings are measured using the Linux tool `time` (`real`).

|           |        Sherly |        xxSherly |
| --------: | ------------: | --------------: |
|  1st run  |        4.055s |          2.561s |
|  2nd run  |        4.055s |          2.304s |
|  3rd run  |        4.066s |          2.549s |
|  **avg**  |    **4.059s** |      **2.471s** |
