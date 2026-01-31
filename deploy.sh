#!/bin/bash
set -e

PROJECT_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
DOCKER_DIR="$PROJECT_ROOT/infra/docker"

# 색상
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

print_step() {
    echo -e "${GREEN}==>${NC} $1"
}

print_warn() {
    echo -e "${YELLOW}[WARN]${NC} $1"
}

print_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

usage() {
    echo "Usage: $0 [OPTIONS] [SERVICES...]"
    echo ""
    echo "Options:"
    echo "  --no-cache       Docker 이미지 캐시 없이 재빌드"
    echo "  --down           컨테이너 중지 후 재시작"
    echo "  --clean          이미지 삭제 후 재빌드 (env 변경 시 사용)"
    echo "  -h, --help       도움말"
    echo ""
    echo "Services:"
    echo "  api              API 서버만"
    echo "  news-analyzer    News Analyzer 서버만"
    echo "  postgres         PostgreSQL만"
    echo "  (생략시 api, news-analyzer)"
    echo ""
    echo "Examples:"
    echo "  $0                      # api, news-analyzer 배포"
    echo "  $0 postgres             # PostgreSQL만 배포"
    echo "  $0 --no-cache api       # API 캐시 없이 재빌드"
    echo "  $0 --down               # api, news-analyzer 재시작"
}

# 옵션 파싱
NO_CACHE=false
DO_DOWN=false
DO_CLEAN=false
SERVICES=()

while [[ $# -gt 0 ]]; do
    case $1 in
        --no-cache)
            NO_CACHE=true
            shift
            ;;
        --down)
            DO_DOWN=true
            shift
            ;;
        --clean)
            DO_CLEAN=true
            shift
            ;;
        -h|--help)
            usage
            exit 0
            ;;
        api|news-analyzer|postgres)
            SERVICES+=("$1")
            shift
            ;;
        *)
            print_error "Unknown option: $1"
            usage
            exit 1
            ;;
    esac
done

# 메인 로직
echo ""
echo "=========================================="
echo "  Stocker Deploy"
echo "=========================================="
echo ""

cd "$DOCKER_DIR"

# .env 파일 체크
if [ ! -f ".env" ]; then
    print_warn ".env 파일이 없습니다. .env.example을 복사합니다."
    cp .env.example .env
fi

# 서비스 미지정 시 기본값: api, analyzer
if [ ${#SERVICES[@]} -eq 0 ]; then
    SERVICES=("api" "news-analyzer")
fi

# 컨테이너 중지
if [ "$DO_DOWN" = true ] || [ "$DO_CLEAN" = true ]; then
    print_step "Stopping containers..."
    docker-compose stop "${SERVICES[@]}"
    docker-compose rm -f "${SERVICES[@]}"
fi

# 이미지 삭제
if [ "$DO_CLEAN" = true ]; then
    print_step "Removing images..."
    for service in "${SERVICES[@]}"; do
        image_name="docker-${service}"
        if docker images | grep -q "$image_name"; then
            docker rmi "$image_name" || true
        fi
    done
fi

# 빌드 옵션
BUILD_ARGS="--build"
if [ "$NO_CACHE" = true ]; then
    BUILD_ARGS="--build --no-cache"
fi

# 배포
print_step "Building and deploying..."
docker-compose up -d $BUILD_ARGS "${SERVICES[@]}"

echo ""
print_step "Deploy complete!"
echo ""
echo "Services:"
docker-compose ps --format "table {{.Name}}\t{{.Status}}\t{{.Ports}}"
echo ""
echo "Endpoints:"
echo "  - API:      http://localhost:28080/health"
echo "  - News Analyzer: http://localhost:28000/health"
echo ""
echo "Logs:"
echo "  docker-compose -f $DOCKER_DIR/docker-compose.yml logs -f"
