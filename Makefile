IMAGE_NAME = "drop-your-time"

run:
	make -C frontend build-release
	make -C backend run

test:
	make -C backend test

fmt:
	make -C frontend fmt
	make -C backend fmt

lint:
	make -C frontend lint

build-image: test lint
	docker build -t $(IMAGE_NAME) .

run-image:
	docker run -p 8080:8080 $(IMAGE_NAME)
