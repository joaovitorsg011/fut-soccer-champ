<div align="center">

# Fut Soccer Champ

**Aplicativo Android para criação e gerenciamento de campeonatos de futebol.**

[![Kotlin](https://img.shields.io/badge/Kotlin-2.0.21-7F52FF?logo=kotlin&logoColor=white)](https://kotlinlang.org)
[![Jetpack Compose](https://img.shields.io/badge/Jetpack%20Compose-Material%203-4285F4?logo=jetpackcompose&logoColor=white)](https://developer.android.com/jetpack/compose)
[![Firebase](https://img.shields.io/badge/Firebase-Auth%20%2B%20Firestore-FFCA28?logo=firebase&logoColor=black)](https://firebase.google.com)
[![minSdk](https://img.shields.io/badge/minSdk-24-3DDC84?logo=android&logoColor=white)](https://developer.android.com)

</div>

---

## Sobre o projeto

Organizar um campeonato amador costuma terminar em planilhas espalhadas e tabelas de classificação
calculadas na mão, onde um placar corrigido raramente chega até a pontuação final.

O **Fut Soccer Champ** centraliza esse fluxo em um único aplicativo: o organizador cria o campeonato,
cadastra os times, monta as rodadas, registra os resultados — e a tabela de classificação se atualiza
sozinha, sempre coerente com os placares registrados.

Projeto desenvolvido para a disciplina de **Projeto de Software**.

## Funcionalidades

| Módulo | O que faz |
|--------|-----------|
| **Autenticação** | Cadastro, login e sessão persistente com Firebase Authentication |
| **Campeonatos** | Criação, edição, listagem e exclusão em cascata |
| **Times** | Cadastro com nome, sigla e escudo, respeitando o limite de participantes |
| **Rodadas** | Criação manual ou geração automática do calendário (round-robin) |
| **Partidas** | Confrontos com data, horário e local; registro e correção de placares |
| **Classificação** | Tabela recalculada a cada resultado, com critérios de desempate |
| **Estatísticas** | Gols, média por partida, maior goleada, melhor ataque e melhor defesa |

## Arquitetura

O projeto segue **MVVM** com **Repository Pattern**, dividido em três camadas independentes:

```
┌─────────────────────────────────────────────┐
│  presentation                               │
│  Telas Compose · ViewModels · Navegação     │
└──────────────────────┬──────────────────────┘
                       ↓
┌─────────────────────────────────────────────┐
│  domain                                     │
│  Regras de negócio puras, sem dependência   │
│  de Android ou Firebase — testáveis na JVM  │
└──────────────────────┬──────────────────────┘
                       ↓
┌─────────────────────────────────────────────┐
│  data                                       │
│  Models · Repositories · Firebase           │
└──────────────────────┬──────────────────────┘
                       ↓
              Authentication · Firestore
```

```
com.futsoccerchamp
├── data
│   ├── model         Championship, Team, Round, Match
│   ├── repository    Auth, Championship, Team, Round, Match
│   └── firebase      Service locator das dependências
├── domain
│   ├── model         Standing
│   └── usecase       CalculateStandings, GenerateRounds
└── presentation
    ├── splash        auth        home
    ├── championship  teams       rounds
    ├── matches       standings   statistics
    ├── common        theme
    └── AppNavigation, Routes
```

### Decisão de projeto: a classificação não é persistida

A tabela poderia ser gravada no banco e atualizada a cada partida, mas isso abre espaço para
divergência: um placar corrigido, uma escrita que falha e a pontuação deixa de refletir os jogos.

Aqui a classificação é **sempre derivada** das partidas encerradas por `CalculateStandingsUseCase`.
Não existe estado duplicado para sincronizar, e qualquer correção de placar se propaga imediatamente
para pontos, saldo e ordenação.

## Regras de classificação

| Sigla | Significado | | Sigla | Significado |
|-------|-------------|-|-------|-------------|
| **P** | Pontos | | **GP** | Gols pró |
| **J** | Jogos | | **GC** | Gols contra |
| **V** | Vitórias | | **SG** | Saldo de gols |
| **E** | Empates | | | |
| **D** | Derrotas | | | |

**Pontuação:** vitória `3` · empate `1` · derrota `0`

**Critérios de desempate**, aplicados em ordem:

```
1. Pontos  →  2. Vitórias  →  3. Saldo de gols  →  4. Gols marcados
```

## Fluxo de navegação

```
Splash
  └── Login / Cadastro
        └── Meus campeonatos
              └── Campeonato  (menu lateral)
                    ├── Classificação      tela inicial
                    ├── Times
                    ├── Rodadas e partidas
                    └── Estatísticas
```

## Modelo de dados

```
users/{userId}
  name · email

championships/{championshipId}
  name · season · teamLimit · description · ownerId · createdAt

teams/{teamId}
  championshipId · name · abbreviation · logoUrl · createdAt

rounds/{roundId}
  championshipId · number

matches/{matchId}
  championshipId · roundId · homeTeamId · awayTeamId
  homeGoals · awayGoals · date · time · place · finished
```

As regras de acesso em [`firestore.rules`](firestore.rules) garantem que cada usuário só alcance
os campeonatos que criou, e os documentos vinculados a eles.

## Tecnologias

| Camada | Stack |
|--------|-------|
| Linguagem | Kotlin 2.0 |
| Interface | Jetpack Compose · Material 3 · Navigation Compose · Coil |
| Arquitetura | MVVM · Repository Pattern · StateFlow |
| Backend | Firebase Authentication · Cloud Firestore |
| Testes | JUnit 4 · kotlinx-coroutines-test |
| Build | Gradle 8.14 · AGP 8.7 · JDK 17 |

## Como executar

**Pré-requisitos:** Android Studio, JDK 17 e um dispositivo ou emulador com Android 7.0 (API 24) ou superior.

```bash
git clone https://github.com/joaovitorsg011/fut-soccer-champ.git
cd fut-soccer-champ
```

### Configuração do Firebase

1. Crie um projeto no [Firebase Console](https://console.firebase.google.com).
2. Adicione um app Android com o package `com.futsoccerchamp`.
3. Baixe o `google-services.json` e coloque em `app/`.
4. Ative **Authentication → Sign-in method → E-mail/senha**.
5. Crie o **Cloud Firestore** e publique o conteúdo de `firestore.rules`.

O projeto compila sem esse arquivo — o plugin do Google Services só é aplicado quando ele existe,
e o app exibe uma tela com as instruções em vez de encerrar. Login e persistência dependem do passo acima.

### Build e testes

```bash
./gradlew installDebug
./gradlew testDebugUnitTest
```

## Testes

As regras de negócio ficam isoladas na camada `domain`, sem dependência de Android ou Firebase,
e são cobertas por testes de unidade que rodam direto na JVM:

- **`CalculateStandingsUseCaseTest`** — pontuação, acúmulo de gols, partidas sem resultado,
  desempate por saldo, desempate por gols marcados e aproveitamento.
- **`GenerateRoundsUseCaseTest`** — número de rodadas com times pares e ímpares, folga por rodada
  e garantia de que nenhum confronto se repete.

## Personalização visual

O fundo das telas de splash e login vem de `app/src/main/res/drawable/bg_auth.xml`.
Para usar uma fotografia, basta substituir o arquivo por `bg_auth.jpg` ou `bg_auth.png` na mesma
pasta — nenhuma alteração de código é necessária.

## Roadmap das entregas

| Entrega | Data | Funcionalidade apresentada |
|:-------:|:----:|----------------------------|
| **AC1** | 14/09 | Autenticação, criação de campeonato, cadastro e listagem de times |
| **AC2** | 13/10 | Rodadas, partidas e registro de resultados |
| **AC3** | 08/11 | Classificação automática com critérios de desempate |
| **Final** | 22/11 | Estatísticas, geração automática de rodadas e refinamento da interface |

## Equipe

| Nome | RA |
|------|-----|
| | |

## Licença

Projeto acadêmico desenvolvido para fins educacionais.
