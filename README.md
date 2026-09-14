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
| **Autenticação** | Login e sessão persistente com Firebase Authentication |
| **Campeonatos** | Criação, edição, listagem e exclusão em cascata |
| **Times** | Cadastro com nome, sigla e escudo escolhido da galeria |
| **Elenco** | Jogadores por time, com número da camisa e posição |
| **Rodadas** | Criação manual ou geração automática do calendário (round-robin) |
| **Partidas** | Confrontos com data, horário e local; registro e correção de placares |
| **Súmula** | Gols por jogador e defesas do goleiro em cada partida |
| **Classificação** | Tabela recalculada a cada resultado, com critérios de desempate |
| **Artilharia** | Ranking de goleadores e de goleiros com mais defesas |
| **Estatísticas** | Gols, média por partida, maior goleada, melhor ataque e melhor defesa |
| **Aparência** | Alternância entre tema claro, escuro e o padrão do sistema, com preferência salva |

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
│   ├── model         Championship, Team, Player, Round, Match
│   ├── repository    Auth, Championship, Team, Player, Round, Match
│   └── firebase      Service locator das dependências
├── domain
│   ├── model         Standing, PlayerRanking
│   └── usecase       CalculateStandings, CalculateRankings, GenerateRounds
└── presentation
    ├── splash        auth        home
    ├── championship  teams       players
    ├── rounds        matches     standings
    ├── rankings      statistics
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
                    ├── Classificação          tela inicial
                    ├── Times ──► Elenco do time
                    ├── Rodadas e partidas
                    ├── Artilharia e defesas
                    ├── Estatísticas
                    └── Tema claro / escuro / sistema
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

players/{playerId}
  championshipId · teamId · name · number · position · photo · createdAt

matches/{matchId}
  championshipId · roundId · homeTeamId · awayTeamId
  homeGoals · awayGoals · date · time · place · finished
  goals   playerId → gols na partida
  saves   playerId → defesas na partida
```

Escudos são redimensionados para 256 px e gravados em Base64 no próprio documento, dispensando
um serviço de arquivos e mantendo o projeto no plano gratuito do Firebase.

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
4. Ative **Authentication → Sign-in method → E-mail/senha** e crie o usuário administrador
   em **Authentication → Users → Adicionar usuário**. O aplicativo não possui tela de cadastro:
   o acesso é restrito às contas criadas no console.
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
- **`CalculateRankingsUseCaseTest`** — soma de gols e defesas, ordenação por total, desempate por
  número de partidas e filtro de goleiros no ranking de defesas.

## Personalização visual

O fundo das telas de splash e login vem de `app/src/main/res/drawable/bg_auth.xml`.
Para usar uma fotografia, basta substituir o arquivo por `bg_auth.jpg` ou `bg_auth.png` na mesma
pasta — nenhuma alteração de código é necessária.

## Roadmap das entregas

| Entrega | Data | Funcionalidade apresentada |
|:-------:|:----:|----------------------------|
| **AC1** | 14/09 | Autenticação, criação de campeonato, cadastro e listagem de times |
| **AC2** | 13/10 | Elenco, rodadas, partidas e registro de resultados |
| **AC3** | 08/11 | Classificação automática com critérios de desempate |
| **Final** | 22/11 | Artilharia, ranking de defesas, estatísticas e refinamento da interface |

## Equipe

| Nome | RA |
|------|-----|
| | |

## Licença

Projeto acadêmico desenvolvido para fins educacionais.
