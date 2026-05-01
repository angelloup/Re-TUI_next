# Termux Integration

Re:T-UI can dispatch non-interactive Termux scripts from its own terminal surface.

This is not a full embedded Termux shell. Termux remains the real shell. Re:T-UI sends script runs to Termux and prints returned output when Termux provides it.

## Commands

- `termux`
- `termux -status`
- `termux -setup`
- `termux -open`
- `termux -run <script_path> [args...]`

Inside the Termux console, you can also type:

- `status`
- `setup`
- `open`
- `run <script_path> [args...]`
- `clear`
- `exit`

## Setup

Run:

`termux -setup`

Re:T-UI will print the setup checklist.

In Termux, enable external commands:

```sh
mkdir -p ~/.termux
echo 'allow-external-apps = true' >> ~/.termux/termux.properties
termux-reload-settings
```

Use a stable script folder:

```sh
mkdir -p ~/retui
nano ~/retui/test.sh
chmod +x ~/retui/test.sh
```

Example test script:

```sh
#!/data/data/com.termux/files/usr/bin/sh
echo "termux is working"
date
```

## Script Aliases

Script aliases use the `-s` alias scope.

Example:

`alias -add -s test /data/data/com.termux/files/home/retui/test.sh`

Then run:

`termux -run test`

The `-s` scope keeps script aliases separate from normal home-screen aliases.

## Script Modules

Script modules let a Termux script feed one of Re:T-UI's terminal-style module panels.

Add a script-backed module:

```text
module -add server termux:/data/data/com.termux/files/home/retui/server-health.sh
```

Refresh it manually:

```text
module -refresh server
```

Show or dock it:

```text
module -show server
module -dock add server
module -dock remove server
```

Remove it from Re:T-UI's module registry:

```text
module -rm server
```

`module -rm` does not delete the Termux script file. It only removes Re:T-UI's registry entry.

For simple modules, printing text to stdout is enough. Re:T-UI will use stdout as the module body after `module -refresh`.

Example:

```sh
#!/data/data/com.termux/files/usr/bin/sh

STATUS="$(ping -c 1 8.8.8.8 >/dev/null 2>&1 && echo 'SERVER: ONLINE' || echo 'SERVER: OFFLINE')"
TIME="$(date)"

printf '%s\n%s\n' "$STATUS" "$TIME"
```

You can also update modules by callback. This is better for scripts that run on their own schedule.

## Arguments

Arguments after the script are passed through to Termux.

Example:

`termux -run test hello world`

## Good Uses

- quick health checks
- backup scripts
- local file summaries
- curl checks
- server status scripts
- small automation helpers

## Limits

- Use non-interactive scripts.
- Do not run `vim`, `nano`, `ssh`, or REPL sessions through `termux -run`.
- Open Termux directly for interactive work.
- Long-running background work may need Android battery optimization disabled for Termux.

## Permission Notes

Termux must be installed from a current source such as F-Droid or GitHub. Old Play Store Termux builds may not expose the run-command bridge.

Android may ask for permission before Re:T-UI can run commands in Termux. Allow Re:T-UI, then retry the command.

## Callback Status

Callbacks from Termux, Tasker, or another automation app can be enabled with a token.

Token commands:

- `retui-token -status`
- `retui-token -show`
- `retui-token -rotate`
- `retui-token -off`

Broadcast action:

`com.dvil.tui_renewed.RETUI_CALLBACK`

Required extras:

- `token`
- `action`

Optional extras:

- `text`
- `title`
- `module`

Safe actions currently accepted:

- `output`
- `notify`
- `module_set`

Example from Termux:

```sh
am broadcast \
  -p com.dvil.tui_renewed \
  -a com.dvil.tui_renewed.RETUI_CALLBACK \
  --es token "YOUR_TOKEN" \
  --es action output \
  --es text "Backup complete"
```

Re:T-UI does not accept arbitrary external command execution through callbacks.

## Callback Module Example

```sh
#!/data/data/com.termux/files/usr/bin/sh

TOKEN="PASTE_RETUI_TOKEN_HERE"
MODULE="server"

STATUS="$(ping -c 1 8.8.8.8 >/dev/null 2>&1 && echo 'SERVER: ONLINE' || echo 'SERVER: OFFLINE')"
TIME="$(date)"

am broadcast \
  -p com.dvil.tui_renewed \
  -a com.dvil.tui_renewed.RETUI_CALLBACK \
  --es token "$TOKEN" \
  --es action module_set \
  --es module "$MODULE" \
  --es text "$STATUS
$TIME"
```

Get the token from Re:T-UI:

```text
retui-token -show
```

## Optional Termux Helper

Create `~/retui/retui-helper.sh`:

```sh
#!/data/data/com.termux/files/usr/bin/sh

RETUI_PACKAGE="com.dvil.tui_renewed"
RETUI_ACTION="com.dvil.tui_renewed.RETUI_CALLBACK"
RETUI_TOKEN="PASTE_RETUI_TOKEN_HERE"

retui_output() {
  am broadcast \
    -p "$RETUI_PACKAGE" \
    -a "$RETUI_ACTION" \
    --es token "$RETUI_TOKEN" \
    --es action output \
    --es text "$*"
}

retui_module() {
  module="$1"
  shift
  am broadcast \
    -p "$RETUI_PACKAGE" \
    -a "$RETUI_ACTION" \
    --es token "$RETUI_TOKEN" \
    --es action module_set \
    --es module "$module" \
    --es text "$*"
}
```

Use it:

```sh
. "$HOME/retui/retui-helper.sh"
retui_module server "SERVER: ONLINE
$(date)"
```

## Tasker Callback Example

Use Tasker's **Send Intent** action:

- Action: `com.dvil.tui_renewed.RETUI_CALLBACK`
- Package: `com.dvil.tui_renewed`
- Target: Broadcast Receiver
- Extra: `token:YOUR_TOKEN`
- Extra: `action:module_set`
- Extra: `module:server`
- Extra: `text:SERVER: ONLINE`

Use `action:output` to print to Re:T-UI output instead of updating a module.
