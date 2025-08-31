from sqlalchemy.orm import Session
from app import models, database

db = database.SessionLocal()

def get_usuario(usuario_id: int):
    return db.query(models.Usuario).filter(models.Usuario.id == usuario_id).first()

def get_latest_concurso():
    return db.query(models.Concurso).order_by(models.Concurso.concurso_id.desc()).first()

def get_all_concursos():
    return db.query(models.Concurso).all()

def get_historico(usuario_id: int):
    return db.query(models.Historico).filter(models.Historico.usuario_id == usuario_id).all()

def get_notificacoes(usuario_id: int):
    usuario = get_usuario(usuario_id)
    if usuario.premium:
        return [{"titulo": "Novo concurso disponível", "mensagem": "Concurso atualizado"}]
    return []
