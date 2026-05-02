# Getting Started

This page is for orientation, not a guided tour. Re:T-UI is built for users who want a command-first Android workstation surface: quiet, local, configurable, and fast once your own aliases and modules are in place.

## First Launch

When Re:T-UI opens, you are dropped into a terminal-style launcher. You can:

- type commands directly
- launch apps by name
- use suggestions under the input field
- open the settings hub with `themer` or `settings`

## Learn From Help

Type:

`help`

That prints the command list and the workstation quickstart. For details about one command, type:

`help <command>`

Examples:

- `help alias`
- `help apps`
- `help wallpaper`
- `help module`

## Basic Navigation

- `themer` opens the settings hub
- `settings -theme` jumps to appearance-related settings
- `settings -music` opens music settings
- `settings -notifications` opens behavior settings, including notification-related XML

## Important Concepts

### Commands

Commands are the fastest way to operate the launcher once you know them.

### Config Files

Power users can still edit launcher behavior through XML and text files in the Re:T-UI folder.

### Presets

Presets let you save a theme state and reuse it later.

### Auto Color

Auto color derives colors from the current wallpaper, but it is separate from manually saved presets.

## Good First Commands

- `help`
- launch an app by typing its name
- `alias -add ll apps -ls`
- `apps -hide <app>`
- `wallpaper -auto`
- `preset -save <name>`
- `module -ls`

## Tip

If something visual does not refresh immediately after a major theme change, run:

`restart`
