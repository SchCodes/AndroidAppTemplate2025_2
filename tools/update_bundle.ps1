# Exemplo de uso do script para atualizar o bundle de dados no Firebase
# Entre na pasta do raiz do projeto:
# cd C:\Users\Eri\Documents\GitHub\AndroidAppTemplate2025_2

# Defina os caminhos dos arquivos
# $xlsx = "C:\Users\Eri\Documents\GitHub\Probabilidade_LotoFacil\BD_full_lotoFacil.xlsx"
$json = "processed/draws.json"

# Defina as variáveis de ambiente do Firebase
$env:FIREBASE_DATABASE_URL = "https://baseforfirebase-f3545-default-rtdb.firebaseio.com"
$env:FIREBASE_STORAGE_BUCKET = "baseforfirebase-f3545.firebasestorage.app"

# Carregue o bundle para o Firebase
python tools/upload_bundle.py `
  --key serviceAccountKey.json `
  --bundle $json `
  --database-url $env:FIREBASE_DATABASE_URL `
  --storage-bucket $env:FIREBASE_STORAGE_BUCKET

# Instruções para executar este script no PowerShell:
# .\tools\update_bundle.ps1

# Certifique-se de que o Python e as dependências necessárias estejam instalados.
# Certifique-se de que o Firebase CLI esteja instalado e configurado.
# Certifique-se de que o arquivo serviceAccountKey.json esteja presente na raiz do projeto.
# Certifique-se de que o arquivo draws.json esteja atualizado na pasta processed.
# Certifique-se de que o Firebase Realtime Database e o Firebase Storage estejam configurados no console do Firebase.
# Certifique-se de que o Firebase Admin SDK esteja instalado no ambiente Python.
