# Stocker Docker 배포

## 디렉토리 구조

```
infra/docker/
├── docker-compose.yml    # 전체 서비스 정의
├── .env.example          # 환경변수 템플릿
├── .env                  # 실제 환경변수 (gitignore)
└── dockerfiles/
    ├── api.Dockerfile      # Multi-stage build
    └── analyzer.Dockerfile # Multi-stage build
```

## 사전 준비

1. **Ollama 실행** (Mac 로컬에서)
   ```bash
   ollama serve
   ```

2. **.env 파일 설정**
   ```bash
   cp .env.example .env
   # 필요시 .env 수정
   ```

## 배포

### 루트에서 deploy.sh 사용 (권장)
```bash
# 전체 배포
./deploy.sh

# 특정 서비스만
./deploy.sh api
./deploy.sh analyzer

# 캐시 없이 재빌드
./deploy.sh --no-cache

# 중지 후 재시작
./deploy.sh --down
```

### 직접 docker-compose 사용
```bash
cd infra/docker

# 전체 실행
docker-compose up -d --build

# 특정 서비스만
docker-compose up -d --build api
```

## 포트 매핑

Stocker 프로젝트는 PostgreSQL 5433 포트 사용 (로컬 기본 postgres 5432와 분리)

| 서비스 | 로컬 직접 실행 | Docker |
|--------|---------------|--------|
| PostgreSQL | 5433 | 5433 |
| API | 8080 | 28080 |
| Analyzer | 8000 | 28000 |

## 유용한 명령어

```bash
# 로그 확인
docker-compose logs -f api
docker-compose logs -f analyzer

# 상태 확인
docker-compose ps

# 전체 중지
docker-compose down

# 전체 중지 + 볼륨 삭제
docker-compose down -v

# 컨테이너 쉘 접속
docker exec -it stocker-api bash
docker exec -it stocker-analyzer bash

# PostgreSQL 접속
psql -h localhost -p 5433 -U stocker -d stocker
```

## 트러블슈팅

### Analyzer가 Ollama에 연결 못할 때
`.env`에서 `OLLAMA_HOST=host.docker.internal` 확인

### DB 연결 실패
`docker-compose ps`로 postgres가 healthy 상태인지 확인

### 이미지 재빌드가 안될 때
```bash
docker-compose build --no-cache api
```

### 빌드 시간이 오래 걸릴 때
Multi-stage build 특성상 첫 빌드는 오래 걸림. 이후 빌드는 Docker 캐시로 빨라짐.
