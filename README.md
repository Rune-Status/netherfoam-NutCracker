# NutCracker
Welcome to the NutCracker Project. This is an attempt at reversing the
obfuscation attempts on RSBot (See [Powerbot.org](http://powerbot.org)).

## Usage
1. Get RSBot-7062.jar
2. Place it in the root directory
3. Run the Main method from this project (org.maxgamer.rs.powerbot.deob.Main)
   * The arguments should be the configuration file to use. Eg `--config config.yml`
   * This is invoking [Deobfuscator](https://github.com/java-deobfuscator/deobfuscator) on the file plus some spice
4. Run in bash `./tools/split.sh`
   * This will take a while the first time you run it

## Result
The output files are placed in the `/deob/` folder:
* `RSBot-7062.jar` is the deobfuscated JAR file
* `classes` is the deobfuscated `.class` files
* `sources` is the resulting `.java` files
