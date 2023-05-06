# xxSherly

A fork of [Sherly](https://github.com/BlyDoesCoding/Sherly), using [xxHash](https://github.com/Cyan4973/xxHash).

![](./images/screenshot.png)

## Introduction

Sherly is a Multithreaded Duplicate File Finder for your Terminal, written in java. You can Easily find duplicate Images, videos as well as any other type of Data. That can be helpful if you run on small storage or just want to keep regular housekeeping.

This fork uses [xxHash](https://github.com/Cyan4973/xxHash) instead of MD5 for performance reasons (see [Speed comparison](#speed-comparison)).
Note that xxHash is not a cryptographic hash function and therefore may produce collisions. That's why the checksum is composed of the xxHash Digest and the filesize.

## Usage

```
usage: xxSherly.jar [options] folder1 folder2 ...
 -c,--color           enable colored output
 -d,--delete          delete all dups except one, without asking first
 -h,--help            show this help message
 -n,--noinput         skip all user input
 -p,--progress        enable progress indicator
 -t,--threads <arg>   override default thread number (defaults to the
                      number of cores)
 -v,--verbose         more verbose output
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

I let Sherly v1.1.4 and xxSherly v1.0 find duplicates in my Music Library (containing `.wav` files) using the following commands:

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
