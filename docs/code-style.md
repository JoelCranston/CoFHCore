# Code style

This port is meant to go upstream as pull requests to the CoFH repos, so every line we add or
change has to read as if CoFH wrote it. The rules below were measured from the untouched
upstream code (branch `1.20.4`, mostly King Lemming's). **When a rule and the surrounding file
disagree, follow the file.**

To check upstream style, run `git show 1.20.4:<path>` or `git grep … 1.20.4`.

## Comments

Upstream barely comments. Across CoFHCore and ThermalCore there are only a few dozen prose
comments. They are one short line, capitalised, and state a fact or a reason:

```java
// Depth offsets to prevent Z-fighting
// Only ever call this in a constructor!
// Vanilla issue causes bamboo to crash if grown close to world height
progress = Math.min(progress + 1, 9); // Ensure that for whatever reason the progress level doesn't go OOB.
```

- **Default to no comment.** A straight API migration (renamed method, new codec, a component
  in place of NBT) gets none: the code says what it does, and the old API is in git history.
- **Comment only a non-obvious reason**, in one line, in upstream's voice. For example,
  `// Negative burn time throws; defer to the furnace_fuels data map.`
- **A major change may take a short block**, a few lines at most. Examples are a rewritten
  subsystem or a workaround for an engine limitation someone might "fix" back.
- **Never**:
  - version tags (`// 1.21:`, `// 26.1.2:`)
  - porting narration ("X was removed upstream, so…", "previously this…")
  - pointers into our docs (`see docs/api-notes-…`, `port plan §…`)
  - references to SPLIGAN's forks, Pyronetics or this session
  - `TODO` notes aimed at the port. Those go in `docs/TODO.md`.
- Javadoc is rare. It goes on API interfaces in `cofh.lib.api` or a genuinely reusable helper, and
  runs a sentence or two.
- Leave upstream's existing comments and commented-out code alone, including its
  `// TODO 1.21 Remove` style notes. Don't add new commented-out code.

## Layout

- **Blank line after the opening brace of every method and constructor body**, including
  one-liners and empty bodies (3,241 upstream method bodies do, 4 don't):

  ```java
  public static <B> DeferredRegisterCoFH<B> create(ResourceLocation registryName, String modid) {

      return new DeferredRegisterCoFH<>(DeferredRegister.create(registryName, modid), modid);
  }

  private FluidHelper() {

  }
  ```

  The same goes for a class body: a blank line after `class X {`. It does **not** apply to lambdas,
  `if`/`for` blocks or anonymous-class bodies.
- A blank line usually separates a method's work from its final `return`. Match the file.
- **A space between an annotation and its parentheses**: `@Mixin (LevelRenderer.class)`,
  `@Inject (method = "…", at = @At (…))`, `@SuppressWarnings ("unchecked")`,
  `@SubscribeEvent (priority = EventPriority.HIGH)` (`@SubscribeEvent (` outnumbers
  `@SubscribeEvent(` 32 to 5 upstream). Annotations without arguments are unchanged (`@Override`, `@SubscribeEvent`).
- Group members with `// region NAME` … `// endregion`, NAME in caps (`HELPERS`, `NBT`,
  `NETWORK`, `DISPLAY`) or an interface name (`// region IFluidHandler`). New members go in
  the matching region.
- 4-space indent, K&R braces, and long lines are fine: upstream doesn't wrap at 100.

## Code

- **Minimal diff.** Replace a removed API in place. Don't reorder, rename or reformat code the
  port didn't need to touch, and keep upstream's parameter names (`levelIn`, `pos`, `state`,
  `pPos`).
- **Imports, not inline qualified names.** `cofh.core.client.CoreKeys.CATEGORY` in a method
  body becomes an import. Import order is IntelliJ's default:
  1. everything else alphabetically (`cofh…`, `com…`, `net…`, `org…`)
  2. a blank line, then `javax…` and `java…`
  3. a blank line, then `import static …`

  No wildcard imports unless the file already uses them.
- `@Nullable` is `javax.annotation.Nullable`. Use `org.jetbrains.annotations.Nullable` only
  where the file already does.
- Prefer explicit types over `var` (upstream uses `var` in about 40 places, mostly model code).
- Constants and helpers go where upstream keeps them: `Constants`, `NBTTags`, `*Helper`,
  `ModIds`. Don't create a new utility class for one method.
- Names follow the file: the `*CoFH` suffix for CoFH subclasses of vanilla types, `ID_*` string
  constants for registry names, and `TAG_*` for NBT keys.
- Build files (`build.gradle`, `gradle.properties`, `*.toml`) follow the same comment rule.
