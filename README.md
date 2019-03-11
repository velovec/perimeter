Perimeter
=========

Battle system for Themis-based CTF contests

## Building and running

Guide to build and run can be found in **DEPLOY.md**

## Management Interface

### SSH Management Interface

Perimeter Server provides SSH command interface for management.
By default it's available on port 1488. 

#### User management

First user that will log in to server will be registered automatically.
All other users should be created manually using command

```
user add <username>
```

Perimeter Server supports both password and public key authorization.
To set SSH public key use command

```
scp -P1488 ~/.ssh/id_rsa.pub <perimeter server>:id_rsa.pub
```

#### Perimeter Server interactive shell

Perimeter Server support both interactive and non-interactive shell

Interactive shell can be used by executing

```
ssh -p1488 <perimeter server>
```

Non-Interactive shell can be used by executing command directly

```
ssh -p1488 -t <perimeter server> <command> <args>
```

#### List of available commands

```
+---------+------------------------------------------------+
| Command | Description                                    |
+---------+------------------------------------------------+
| scp     | Copy files over SSH                            |
| vulnbox | VulnBox manager                                |
| agent   | Manage remote agents                           |
| flag    | Flag processor (submit flag, view queue stats) |
| clear   | Erases the screen with the background colour   |
| setenv  | Set environment variables                      |
| team    | Manage teams                                   |
| acl     | Manage HAProxy ACLs                            |
| users   | Manage users settings                          |
| exit    | Closes current session                         |
| help    | Print available commands with description      |
| passwd  | Change current user password                   |
| service | Manage services                                |
| haproxy | Manage HAProxy configuration                   |
| backend | Manage HAProxy backends                        |
| exploit | Manage exploits                                |
| themis  | Themis integration                             |
| getenv  | List environment variables                     |
+---------+------------------------------------------------+
```

##### SCP

SCP is used to provide ability to manage configuration and exploits using `scp`

##### VulnBox

##### Agent

List available remote agents (both executors and configurators) and tasks running

##### Flag

Flag queue management and statistics

##### Team

Team management and list

##### ACL

HAProxy ACL list

##### Users

User management

##### Service

Service list and status

##### HAProxy

HAProxy status and configuration management

You can update HAProxy configuration using SCP

```
scp -P1488 examples/haproxy.yml.example <perimeter server>:config/haproxy.yml
```

And apply this config using Perimeter command 

```
haproxy apply
```

HAProxy backend status can be checked using Perimeter command

```
haproxy status
```

##### Backend

HAProxy backend list

##### Exploit

Exploit execution and management

Exploits can be added/updated using SCP

```
scp -P1488 exploit.file <perimeter server>:exploits/<exploit type>/<exploit name>
```

For example

```
scp -P1488 examples/exploit.example.py <perimeter server>:exploits/python/example.py
```

By default all added exploits will have **LOW** priority, priority can be adjusted using command

```
exploit set_priority <name> <priority>
```

Exploits will be automatically executed with interval specified in Perimeter Server config (by default 1 minute),
or can be executed manually using command

```
exploit exec <name>
```

##### Themis

Themis extended integrations:

* `sync` - Team list synchronization
* `flag_info` - Flag info requests
* `status` - Contest status
* `public_key` - JWT public key update
* `submit` - Manual flag submission