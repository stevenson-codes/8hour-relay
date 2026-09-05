# 8hour-relay

## Running with Docker

Start postgres, mosquitto, backend, and both frontends together:

```
docker compose up -d --build
```

Check everything is up:

```
docker compose ps
```

Stop everything:

```
docker compose down
```

## Public board vs. admin board

There are two frontend containers built from the same codebase, split by the
`VITE_READ_ONLY` build flag:

- **`frontend-public`** — `http://localhost` (port 80, bound to all
  interfaces). Read-only: no Add Team, Edit, Start/Stop Race, or Clear
  buttons. This is the one safe to put on the event LAN or tunnel to the
  internet — its nginx config (`frontend/nginx.public.conf`) also rejects
  every non-GET/HEAD request to `/api/` at the proxy level, so even a direct
  API call (not just the hidden buttons) can't mutate race data through it.
- **`frontend-admin`** — `http://localhost:3000`, bound to `127.0.0.1` only
  (not reachable from the LAN or a tunnel). Has every control: Add Team,
  Edit, Start/Stop Race, Clear Lap Records, Clear All.

Postgres (`5433`), mosquitto (`1884`), and the backend (`8080`) are also
bound to `127.0.0.1` only — nothing but the two frontend containers needs to
reach them, and they talk to each other over the internal Docker network
regardless of these host bindings.

## Exposing the public board to the internet (ngrok)

Use this to let people outside your local network view the board (e.g. at an
event). Only ever tunnel port **80** (`frontend-public`) — never port 3000.

1. Make sure the containers are running (`docker compose up -d`).
2. Start a tunnel to port 80:
   ```
   ngrok http 80
   ```
3. Share the `https://....ngrok-free.dev` URL shown under "Forwarding". Leave the terminal window open for the URL to keep working.
4. A custom/reserved subdomain requires a paid ngrok plan — the free random URL stays the same as long as the tunnel isn't restarted.
5. Run race control (Start/Stop, Add Team, Edit, Clear) from `http://localhost:3000` on the machine running Docker — never expose that port.

## Manual IP

169.254.81.170

## Tests still to do

- Test manual start stop switch.
- Test handoff enabled window
