.PHONY: dev build build-release lint fmt fmt-check clean

install-npm-deps:
	npm install ci

dev: install-npm-deps
	npx shadow-cljs watch app

build: install-npm-deps
	npx shadow-cljs compile app

build-release: clean install-npm-deps
	npx shadow-cljs release app

lint:
	clojure -M:lint

fmt:
	clojure -M:fmt fix

fmt-check:
	clojure -M:fmt check

clean:
	rm -rfv target
