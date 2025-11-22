# Admin Update Function (HTTP, Python)

Função HTTP para acionar a atualização da base (bundle/metadata) com verificação de token admin.

## Estrutura
```
cloud_functions/admin_update/
  main.py              # código da função
  requirements.txt     # dependências
```

## Pré-requisitos
- Python 3.10+ instalado localmente.
- `gcloud` ou `firebase-tools` configurado no projeto.
- Uma conta de serviço com permissão em RTDB/Storage (use sua `serviceAccountKey.json` apenas para testes locais; **não** faça commit).
- Custom claim `admin` definido no usuário que vai acionar a função.

## Deploy (gcloud, Cloud Functions Gen2)
```
cd cloud_functions/admin_update
gcloud functions deploy update_bundle \
  --runtime python312 \
  --region=us-central1 \
  --entry-point update_bundle \
  --trigger-http \
  --allow-unauthenticated=false
```

## Teste
- No app, defina `admin_update_url` (strings.xml) com a URL retornada.
- Faça login com usuário admin e acione “Atualizar base (admin)”. O app envia `Authorization: Bearer <idToken>`.

## Observação
O trecho `update_dataset()` em `main.py` está como TODO para plugar sua rotina real (ex.: rodar scraping, salvar `processed/draws.json` no Storage e atualizar `/metadata/latest` no RTDB).
