.PHONY: build test fmt fmt-check run clean

dev:
	dune exec -w backend

build:
	dune build

lock-deps:
	opam lock .

test:
	dune runtest -f --display=short

fmt:
	dune fmt

fmt-check:
	dune fmt --preview

run:
	dune exec backend

clean:
	dune clean
