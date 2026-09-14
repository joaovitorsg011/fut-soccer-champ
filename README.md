# Fut Soccer Champ

Aplicativo Android para **criação e gerenciamento de campeonatos de futebol**: cadastro de times,
organização de rodadas e partidas, registro de resultados e cálculo automático da tabela de classificação.

## Tecnologias

- Kotlin
- Jetpack Compose + Material 3
- Firebase Authentication
- Cloud Firestore
- MVVM + Repository Pattern
- Navigation Compose, Coil

## Arquitetura

```
presentation  → Telas Compose, ViewModels, navegação
domain        → Regras de negócio (classificação, geração de rodadas)
data          → Models, Repositories, integração com o Firebase
```

A tabela de classificação **nunca é persistida**: ela é sempre calculada a partir das partidas
por `CalculateStandingsUseCase`, o que impede divergência entre placar e pontuação.

## Fluxo do app

```
Splash → Login / Cadastro → Meus campeonatos → Campeonato
                                                  ├── Classificação (tela inicial)
                                                  ├── Times
                                                  ├── Rodadas e partidas
                                                  └── Estatísticas
```

Dentro do campeonato a navegação é feita pelo **menu lateral** (ícone ☰ na barra superior).

## Regras de classificação

| Sigla | Significado |
|-------|-------------|
| P | Pontos |
| J | Jogos |
| V | Vitórias |
| E | Empates |
| D | Derrotas |
| GP | Gols pró |
| GC | Gols contra |
| SG | Saldo de gols |

- Vitória = 3 pontos, empate = 1, derrota = 0.
- Desempate: pontos → vitórias → saldo de gols → gols marcados.

## Configuração do Firebase

1. Crie um projeto no [Firebase Console](https://console.firebase.google.com).
2. Adicione um app Android com o package `com.futsoccerchamp`.
3. Baixe o `google-services.json` e coloque em `app/google-services.json`.
4. Habilite **Authentication → E-mail/senha**.
5. Crie o **Cloud Firestore** e publique as regras de `firestore.rules`.

O projeto compila sem o `google-services.json` (o plugin só é aplicado quando o arquivo existe),
mas o login e a persistência só funcionam depois desse passo.

## Como rodar

```bash
./gradlew installDebug      # instala no dispositivo/emulador conectado
./gradlew testDebugUnitTest # roda os testes das regras de negócio
```

## Personalizar o fundo do splash e do login

Substitua `app/src/main/res/drawable/bg_auth.xml` por uma imagem `bg_auth.jpg` ou `bg_auth.png`
na mesma pasta. O código não precisa ser alterado.

## Modelo de dados (Firestore)

```
users/{userId}              name, email
championships/{id}          name, season, teamLimit, description, ownerId, createdAt
teams/{id}                  championshipId, name, abbreviation, logoUrl, createdAt
rounds/{id}                 championshipId, number
matches/{id}                championshipId, roundId, homeTeamId, awayTeamId,
                            homeGoals, awayGoals, date, time, place, finished
```

## Entregas

| Entrega | Data | Funcionalidade |
|---------|------|----------------|
| AC1 | 14/09 | Autenticação, criação de campeonato, cadastro e listagem de times |
| AC2 | 13/10 | Rodadas, partidas e registro de resultados |
| AC3 | 08/11 | Classificação automática com critérios de desempate |
| Final | 22/11 | App completo, estatísticas e geração automática de rodadas |
