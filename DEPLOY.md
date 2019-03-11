## Building

```mvn clean install```

## Running 

Create Docker network

```
docker network create --subnet 10.20.30.0/24 perimeter
```

### Prerequisites

Start PostgreSQL server

```
docker run -d --name postgres --net perimeter -e POSTGRES_PASSWORD=P@ssw0rd postgres
```

Create database

```
docker exec -it postgres psql -U postgres -c 'CREATE DATABASE perimeter;'
```

### Running Themis Mock Server

Create config for Themis Mock Server:

```
themis:
    network:
        internal: 10.20.30.0/31  # Internal network
        team: 10.20.30.32/27     # Team network range
        team-subnet-cidr: 31     # Team subnet CIDR
    teams:                       # Team definitions
        - id: 1
          name: v0rt3x
          guest: false
        - id: 2
          name: MagicHat
          guest: true
    duration: 300000             # Contest duration in miliseconds
    jwt:
        algorithm: EC256         # JWT Algorithm
        public-key: |
            -----BEGIN PUBLIC KEY-----
            MFkwEwYHKoZIzj0CAQYIKoZIzj0DAQcDQgAEQgb5npLHd0Bk61bNnjK632uwmBfr
            F7I8hoPgaOZjyhh+BrPDO6CL6D/aW/yPObXXm7SpZogmRwGROcOA3yUleg==
            -----END PUBLIC KEY-----
        private_key: |
            -----BEGIN PRIVATE KEY-----
            MIGHAgEAMBMGByqGSM49AgEGCCqGSM49AwEHBG0wawIBAQQgPGJGAm4X1fvBuC1z
            SpO/4Izx6PXfNMaiKaS5RUkFqEGhRANCAARCBvmeksd3QGTrVs2eMrrfa7CYF+sX
            sjyGg+Bo5mPKGH4Gs8M7oIvoP9pb/I85tdebtKlmiCZHAZE5w4DfJSV6
            -----END PRIVATE KEY-----
```

Start Themis Mock Server

```
docker run -d --name themis --net perimeter -v <path to config>:/opt/themis/themis.yml v0rt3x/themis-mock-server
```

### Running Perimeter Server

Create config for Perimeter Server:

```
spring:
    datasource:
        type: com.zaxxer.hikari.HikariDataSource
        url: jdbc:postgresql://postgres:5432/perimeter
        username: postgres
        password: P@ssw0rd

perimeter:
    shell:
        host: 0.0.0.0
        port: 1488
        host-key: master.key
        auth-storage:
            path: auth.storage
            key: 706572696d65746572
    team:
        base-network: 10.20.30.32                  # Team network range
        subnet-cidr: 31                            # Team subnet CIDR
        vulnbox-address: 2                         # VulnBox address in subnet
        internal-ip: 127.0.0.1                     # VulnBox internal IP
        stats-port: 1936                           # HAProxy statistics port
        production-backend: prod                   # HAProxy production backend name
    flag:
        ttl: 300                                   # Flag time-to-live
        pattern: ^(?<flag>[a-fA-F0-9]{32}=)$       # Flag pattern
        jwt:
            enabled: false                         # Are flags wrapped in JWT
            algorithm: EC256                       # JWT algorithm
            pattern: ^VolgaCTF\{(?<flag>.*)\}$     # JWT wrapper pattern
    exploit:
        execution-interval: 60000                  # Exploit execution interval in miliseconds
    themis:
        host: themis                               # Themis IP
        port: 5000                                 # Themis port
        protocol: http                             # Themis protocol
        integration-enabled: false                 # THemis extended integrations
    agent:
        timeout: 20000                             # Perimeter Agent timeout
        delete-after: 100000                       # Perimeter Agent auto-delete interval

```

Start Perimeter Server

```
docker run -d --name perimeter --net perimeter -v <path to config>:/opt/perimeter/perimeter.yml v0rt3x/perimeter-server
```

### Running Perimeter Configuration Agent

Create Perimeter Configuration Agent config:

```
perimeter:
    server:
        host: perimeter                                 # Perimeter Server host
        port: 8080                                      # Perimeter Server port
        protocol: http                                  # Perimeter Server protocol
    configurator:
        templates-path: config_templates                # Path to configuration templates
        configurators:
            haproxy:                                    # Configurator definition for HAProxy
                config-path: /tmp/haproxy.cfg           # Path to store HAProxy cOnfig
                template-file: haproxy.cfg.j2           # Template for HAProxy config
                apply-command: docker restart haproxy   # Command to apply HAProxy config
                overrides:                              # Configuration overrides
                    host: 0.0.0.0
```

Copy config templates to any directory and start Perimeter Configurator Agent

```
docker run -d --name perimeter-configurator --net perimeter \
  -v <path to config>:/opt/perimeter/perimeter-configurator.yml \
  -v <path to config teplates>:/opt/perimeter/config_templates \
  v0rt3x/perimeter-configurator
```

### Running Perimeter Executor Agent

Create Perimeter Executor Agent config

```
perimeter:
    server:
        host: perimeter
        port: 8080
        protocol: http
    executor:
        tmp-directory: /tmp/perimeter/exploit           # Path to store exploits and execution logs
        execution-timeout: 120000                       # Exploit execution timeout
        command-line:                                   # Commands to run different types of exploits
          python: python %exploit% %team%
          go: go run %exploit% %team%
          java: javac %exploit%; java %exploit% %team%
          shell: bash %exploit% %team%
```

Start Perimeter Executor Agent

```
docker run -d --name perimeter-executor --net perimeter -v <path to config>:/opt/perimeter/perimeter-executor.yml v0rt3x/perimeter-executor
```
