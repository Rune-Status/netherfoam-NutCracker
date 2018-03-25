# New Approach
Retrieving the deobfuscated code isn't working, but now I know:
* It's probably Allatori used to obfuscate the code
* This guy seems to be working on Allatori flow deobfuscation https://github.com/java-deobfuscator/deobfuscator/pull/184
  * Which is a major pain
* The code is signed. It can be unsigned by:
  * Removing META-INF RSA keys
  * Removing the SHA signatures from the MANIFEST.MF file (Maybe not required?)
* Everything in z/ appears to be a Dependency of Powerbot
  * These dependencies are probably publicly available
  * Eg `z/nul` appears to be `org.objectweb.asm.Opcodes`

The deobfuscator / decompiler appear to miss a handful of things:
* `this` is incorrectly referenced as `a` (It's sometimes suffixed by a number)
  * Hopefully a Transformer could fix this
* Interfaces have their `public static final` fields assigned inside a `static{}` block
  * These assignments should be inlined with their respective declarations
* The deobfuscators crash on some methods (both Fernflower & CFR)
  * Possible that this is triggered by ASM?
  * Stack overflows still occur with 16M stack instead of 512K
* Some classes are marked as static and this is a compile error

So, perhaps it's time for a new approach. To only edit the bot, instead of
deobfuscate the whole thing.

## Yo Dawg, I Heard You Like Agents
I could inject the process with two Java Agents. See here: https://stackoverflow.com/questions/872657

Using ASM: https://stackoverflow.com/questions/45180625

What would I do? Well, I could:
* Remove methods which create ads
    * Client downloads a file `control.ini` defining ads
* Remove the time limit
    * Mark myself as a VIP subscriber automatically

There appears to be a code block which determines my properties and puts them in a Map<String, String> (or maybe enviornment) inside ScriptList.java
```
var3_4.field81.put("user.id", (var7_15 = (var4_8 = NetworkAccount.Method2998()).Method3004()) != false ? Integer.toString(var4_8.Method3002()) : "0");
var3_4.field81.put("user.name", var7_15 != false ? var4_8.Method2997() : "");
var3_4.field81.put("script.local", Boolean.toString(var1_1.field689));
var3_4.field81.put("user.vip", Boolean.toString(var7_15 != false && var4_8.Method2992(NetworkAccount.1.field3942) != false));
```
