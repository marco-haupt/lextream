# Lextream: The Ultimate Lexer Toolchain 🚀

Welcome to **Lextream** — the project that will *lex* you learn the magic of lexer generation! Whether you're just starting out or you dream in finite automata (who doesn't?), this toolchain was crafted to make core concepts click — with code that's clean, readable, and just the right amount of silly.

## 📚 Overview

**Lextream** is a multi-module Maven project that walks you through the stages of lexer generation. It’s designed for education and exploration — lightweight, readable, and free of unnecessary complexity. No performance wizardry or feature bloat here — just solid, honest DFA-building and tokenizing.

> ⚠️ **Heads-up:** This project depends on another GitHub repo of mine called [redeggs](https://github.com/marco-haupt/redeggs). It handles regular eggspression parsing — the yolk at the center of this lexical omelette.  
Before Lextream can crack open your tokens, **you'll need to clone `redeggs`, complete a small quest detailed in its README, and run `mvn install`.**
Think of it as a warm-up eggsercise — prep work before we scramble the input and hatch some tokens. 🐣

### 🎯 Dependency Setup

Clone and install the required `redeggs` project first:

```bash
git clone https://github.com/marco-haupt/redeggs.git
cd redeggs
# 🥚 Complete the small quest in its README (yes, it's intentional)
mvn clean install
```

Once that’s done, you're ready to bring lexical order to the universe.

## 🛠️ Lextream's Lexer Toolchain

Lextream is made up of four modules — together forming a *lexecution pipeline*:

1. **lexpress**:  
   Reads regular expressions from files and "expresses" them as **minimal DFAs**.  
   *Turn those regexes into machines that run at the speed of... well, a DFA!*

2. **lexemble**:  
   Combines multiple DFAs into a single **lexer specification**.  
   *Some DFAs just want to be part of something bigger.*

3. **lexify**:  
   Uses a lexer specification to tokenize input into a **stream of tokens**.  
   *Making sense out of strings—one token at a time.*

4. **lexport**:  
   A shared library to read/write the formats used between tools.  
   *So your DFAs, specs, and streams don’t get lost in translation.*

All modules except `lexport` build into CLI tools you can run from the terminal.

**💡 Pro tip:** Every tool supports `--help` for usage info and argument descriptions.

## 🚦 Getting Started

### ✅ Prerequisites

Make sure you have the following:

- Java 8 or higher
- Maven (to manage those dependencies)
- The `redeggs` project (installed as described above)
- A love for lexical adventures (and maybe a bit of caffeine)

### 📥 Cloning the Repo

To get your hands on Lextream, clone the repo to your local machine:

```bash
git clone https://github.com/marco-haupt/lextream.git
cd lextream
```

### 🧱 Building the Project

Once you've got it on your system, you can build the project using Maven:

```bash
mvn clean install
```

This compiles all modules and gets the CLI tools ready for action.

### ▶️ Running the Tools

Each CLI module can be run like so:

```bash
mvn exec:java -pl lexpress -Dexec.mainClass="com.lextream.lexpress.Main"
mvn exec:java -pl lexemble -Dexec.mainClass="com.lextream.lexemble.Main"
mvn exec:java -pl lexify -Dexec.mainClass="com.lextream.lexify.Main"
```

For argument info, use:

```bash
mvn exec:java -pl lexify -Dexec.args="--help"
```

Customize arguments as needed based on your input/output files.

## 🎓 Why This Project Exists

Lexer generators are usually buried deep in intimidating compiler frameworks. **Lextream** pulls them back into the daylight, breaking the process into clear, focused steps. It’s ideal for teaching, tinkering, and finally answering that age-old question:

> "Wait... how does `lex` actually work?"

The goal? Clarity over cleverness. Readability over rocket science. And yes — a well-placed pun or two along the way.

## 🤝 Contributing

Found a bug? Have a better pun? Want to add a feature, refactor something, or improve examples? We welcome pull requests, issues, and all kinds of lexical mischief.

- Fork the repo
- File an issue
- Submit a pull request

We don’t bite. We *lex*.

## 🗓️ Versioning

Current version: 0.0.1-SNAPSHOT  
Expect changes. Especially once students start poking it.

---

Thanks for checking out **Lextream**!  
May your states be minimal and your tokens always valid.
