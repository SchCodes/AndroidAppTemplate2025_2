from fastapi import FastAPI
from app import crud, models, database, schemas

app = FastAPI(title="LotoLab API")

@app.get("/usuarios/{usuario_id}")
def get_usuario(usuario_id: int):
    usuario = crud.get_usuario(usuario_id)
    return usuario

@app.get("/concursos/latest")
def get_latest_concurso():
    concurso = crud.get_latest_concurso()
    return concurso

@app.get("/concursos/all")
def get_all_concursos():
    concursos = crud.get_all_concursos()
    return concursos

@app.get("/historico/{usuario_id}")
def get_historico(usuario_id: int):
    historico = crud.get_historico(usuario_id)
    return historico

@app.get("/notificacoes/{usuario_id}")
def get_notificacoes(usuario_id: int):
    notificacoes = crud.get_notificacoes(usuario_id)
    return notificacoes
