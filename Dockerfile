############################################################
## backend-builder

FROM ocaml/opam:ubuntu-lts-ocaml-5.1 AS backend-builder

WORKDIR /app

RUN sudo apt-get install -y libev-dev pkg-config libffi-dev libssl-dev libsqlite3-dev

COPY backend/backend.opam .
COPY backend/backend.opam.locked .
RUN opam install --deps-only .
RUN eval $(opam env)

COPY --chown=opam backend/. .
RUN eval $(opam env) && dune build


############################################################
## frontend-builder

FROM theasp/clojurescript-nodejs:shadow-cljs AS frontend-builder

WORKDIR /app

COPY frontend/package.json .
COPY frontend/package-lock.json .
RUN npm ci

COPY frontend/. .

RUN make build-release


############################################################
## release image

FROM ocaml/opam:ubuntu-lts-ocaml-5.2

WORKDIR /app

RUN sudo apt-get install -y libev-dev pkg-config libffi-dev libssl-dev libsqlite3-dev

# Copy only the executable files from the builder stage
COPY --from=backend-builder  /app/_build/default/bin/main.exe /app/.
COPY --from=frontend-builder /app/resources/public            /app/public
COPY --from=frontend-builder /app/target/js                   /app/public/js

ENV JS_PATH="/app/public/js" \
    HTML_PATH="/app/public"

EXPOSE 8080

ENTRYPOINT ["./main.exe"]
