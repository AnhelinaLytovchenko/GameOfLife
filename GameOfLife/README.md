# 🧬 Game of Life — Паралельна реалізація

Conway's Game of Life з послідовним і паралельним алгоритмом (Java Fork/Join Framework).

## Структура проекту

```
src/com/gameoflife/
├── Main.java                 — точка входу
├── GameGrid.java             — послідовна реалізація (sequential)
├── ParallelGameGrid.java     — паралельна реалізація (parallel)
├── GenerationTask.java       — Fork/Join задача для одного діапазону рядків
├── GameOfLifeBenchmark.java  — порівняння sequential vs parallel
├── GameFrame.java            — головне вікно (UI)
└── GamePanel.java            — панель відображення сітки
```

## Як запустити

### IntelliJ IDEA
1. **File → Open** → вибрати папку проекту
2. Переконатися, що встановлено JDK 17+
3. Запустити конфігурацію **Main (Game of Life)** або **Benchmark**

### Командний рядок
```bash
# Компіляція
mkdir out
javac --release 17 -d out src/com/gameoflife/*.java

# Запуск гри
java -cp out com.gameoflife.Main

# Запуск бенчмарку
java -cp out com.gameoflife.GameOfLifeBenchmark
```

## Архітектура паралелізму

```
step() у ParallelGameGrid
        │
        ▼
   ForkJoinPool
        │
   GenerationTask(0, rows)
        │
   ┌────┴────┐
   │         │
task(0,mid) task(mid,rows)   ← fork
   │         │
  ...       ...              ← рекурсія до THRESHOLD=8 рядків
   │         │
computeDirectly()            ← base case, записує nextGrid
        │
   AtomicInteger.addAndGet() ← підрахунок популяції без locks
```

## Керування у грі

| Дія | Опис |
|-----|------|
| `Пробіл` | Старт / Пауза |
| Клік мишею | Увімкнути/вимкнути клітину |
| Перетягування | Малювати клітини |
| ⚡ Паралельний | Перемкнути режим обчислень |
| 📊 Бенчмарк | Запустити порівняння швидкості |
