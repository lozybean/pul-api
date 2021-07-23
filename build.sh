source .env
SQL_MIGRATION_IMAGE="pul-sql-migration:${VERSION}"
APP_IMAGE="pul_api:${VERSION}"

cd sql-migration || {
  echo "dir:sql-migration not exists!"
  exit 1
}
docker build -t "${SQL_MIGRATION_IMAGE}" . || {
  echo "build sql-migration image failed!"
  exit 1
}

cd ..
mvn clean package -Dmaven.test.skip=true -Drevision="${VERSION}" || {
  echo "maven package failed!"
  exit 1
}

docker build -t "${APP_IMAGE}" . || {
  echo "build api image failed!"
  exit 1
}

