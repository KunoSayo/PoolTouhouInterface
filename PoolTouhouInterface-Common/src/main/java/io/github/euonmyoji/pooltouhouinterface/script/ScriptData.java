package io.github.euonmyoji.pooltouhouinterface.script;

import io.github.euonmyoji.pooltouhouinterface.PthData;
import io.github.euonmyoji.pooltouhouinterface.game.ICollide;
import io.github.euonmyoji.pooltouhouinterface.script.callback.Kill;
import io.github.euonmyoji.pooltouhouinterface.script.callback.MoveUp;

import java.io.DataInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

/**
 * @author yinyangshi
 */
public class ScriptData {
    public final String name;
    public HashMap<String, ScriptFunction> functions = new HashMap<>();
    public int version;
    public short dataCount;
    public ScriptFunction tickFunction;


    public ScriptData(PthData pthData, String name, Path scriptPath) throws IOException {
        this.name = name;
        try (DataInputStream in = new DataInputStream(Files.newInputStream(scriptPath))) {
            this.version = in.readInt();
            this.dataCount = (short) in.readUnsignedByte();
            while (in.available() > 0) {
                String funName = in.readUTF();
                ScriptFunction function = loadFunction(funName, pthData, in);
                functions.put(funName, function);
            }
            tickFunction = functions.remove("tick");
        }
    }

    @SuppressWarnings("ConstantConditions for trust script compiler.")
    private ScriptFunction loadFunction(String funName, PthData pthData, DataInputStream in) throws IOException {
        byte cur;
        ScriptFunction scriptFunction = new ScriptFunction();
        scriptFunction.maxStack = 0;
        LinkedList<IScriptCommand> commands = new LinkedList<>();
        LinkedList<Loop> loopMarks = new LinkedList<>();
        int loops = 0;
        if(PthData.debug) {
            System.out.println("[loading function " + funName + " in script " + this.name);
        }
        loop:
        while (true) {
            cur = in.readByte();
            switch (cur) {
                case 0: {
                    if (PthData.debug) {
                        System.out.println("end");
                    }
                    if (loops > 0) {
                        commands.add(runner -> {
                            int l = 0;
                            for (int i = runner.function.loops.length - 1; i >= 0; i--) {
                                Loop loop = runner.function.loops[i];
                                if (loop.pointer < runner.context.pointer) {
                                    if (loop.type == Loop.Type.START) {
                                        if(l == 0) {
                                            runner.context.pointer = loop.pointer;
                                            return;
                                        } else {
                                            l -= 1;
                                        }
                                    } else {
                                        l += 1;
                                    }
                                }
                            }
                        });
                        loopMarks.add(new Loop(Loop.Type.END, commands.size() - 1));
                        loops -= 1;
                    } else {
                        break loop;
                    }
                    break;
                }
                case 1: {
                    if (PthData.debug) {
                        System.out.println("loop");
                    }
                    loops += 1;
                    loopMarks.add(new Loop(Loop.Type.START, commands.size() - 1));
                    break;
                }
                case 3: {
                    commands.add(loadF32ToCalcStack(in, scriptFunction));
                    break;
                }
                case 5: {
                    if (PthData.debug) {
                        System.out.println("break");
                    }
                    ScriptDataSupplier b = loadF32(in, scriptFunction);
                    commands.add(runner -> {
                        double value = b.get(runner);
                        if (value >= 1.0) {
                            int times = (int) Math.floor(value);
                            int loopCount = 0;
                            int loopIndex = 0;
                            while (loopIndex < runner.function.loops.length) {
                                Loop loop = runner.function.loops[loopIndex];
                                if (loop.pointer < runner.context.pointer) {
                                    if (loop.type == Loop.Type.START) {
                                        loopCount += 1;
                                    } else {
                                        loopCount -= 1;
                                    }
                                } else {
                                    break;
                                }
                                ++loopIndex;
                            }
                            for (int i = 0; i < times && loopCount > 0; i++) {
                                int layer = 0;
                                while (loopIndex < runner.function.loops.length) {
                                    Loop loop = runner.function.loops[loopIndex];
                                    if (loop.pointer > runner.context.pointer) {
                                        if (loop.type == Loop.Type.START) {
                                            layer += 1;
                                        } else {
                                            if (layer == 0) {
                                                runner.context.pointer = loop.pointer;
                                                loopCount -= 1;
                                                break;
                                            }
                                            layer -= 1;
                                        }
                                    }
                                    ++loopIndex;
                                }
                            }
                        }
                    });
                    break;
                }
                case 6: {
                    if (PthData.debug) {
                        System.out.println("wait ");
                    }
                    ScriptDataSupplier b = loadF32(in, scriptFunction);
                    commands.add(runner -> runner.context.wait = (int) Math.floor(b.get(runner)));
                    break;
                }
                case 10: {
                    if (PthData.debug) {
                        System.out.println("move up");
                    }
                    ScriptDataSupplier b = loadF32(in, scriptFunction);
                    commands.add(runner -> runner.pthData.scriptCallback.add(new MoveUp(b.get(runner))));
                    break;
                }
                case 11: {
                    if (PthData.debug) {
                        System.out.println("summon enemy");
                    }
                    String name = in.readUTF();
                    ScriptDataSupplier xS = loadF32(in, scriptFunction);
                    ScriptDataSupplier yS = loadF32(in, scriptFunction);
                    ScriptDataSupplier hpS = loadF32(in, scriptFunction);
                    int collideType = in.readUnsignedByte();
                    ScriptDataSupplier[] argsS = new ScriptDataSupplier[ICollide.getCollideArgCount(collideType)];
                    for (int i = 0; i < argsS.length; i++) {
                        argsS[i] = loadF32(in, scriptFunction);
                    }

                    String scriptAi = in.readUTF();
                    ScriptDataSupplier[] aiArgsArrayS;
                    {
                        List<ScriptDataSupplier> aiArgs = new ArrayList<>();
                        ScriptDataSupplier s;
                        while ((s = loadF32(in, scriptFunction)) != null) {
                            aiArgs.add(s);
                        }
                        if (aiArgs.isEmpty()) {
                            aiArgsArrayS = null;
                        } else {
                            aiArgsArrayS = aiArgs.toArray(new ScriptDataSupplier[0]);
                        }
                    }

                    ScriptData scriptData = scriptAi.equals(this.name) ? this : pthData.getScript(scriptAi);

                    commands.add(runner -> runner.pthData.summonEnemy(name, xS.get(runner), yS.get(runner), hpS.get(runner),
                            collideType, argsS, scriptData, runner, aiArgsArrayS));
                    break;
                }
                case 12: {
                    if (PthData.debug) {
                        System.out.println("summon bullet");
                    }
                    String name = in.readUTF();
                    ScriptDataSupplier x = loadF32(in, scriptFunction);
                    ScriptDataSupplier y = loadF32(in, scriptFunction);
                    ScriptDataSupplier scale = loadF32(in, scriptFunction);
                    ScriptDataSupplier angle = loadF32(in, scriptFunction);
                    int collideType = in.readUnsignedByte();
                    ScriptDataSupplier[] collideArgs = new ScriptDataSupplier[ICollide.getCollideArgCount(collideType)];
                    for (int i = 0; i < collideArgs.length; i++) {
                        collideArgs[i] = loadF32(in, scriptFunction);
                    }

                    String scriptAi = in.readUTF();
                    ScriptDataSupplier[] aiArgsArrayS;
                    {
                        List<ScriptDataSupplier> aiArgs = new ArrayList<>();
                        ScriptDataSupplier s;
                        while ((s = loadF32(in, scriptFunction)) != null) {
                            aiArgs.add(s);
                        }
                        if (aiArgs.isEmpty()) {
                            aiArgsArrayS = null;
                        } else {
                            aiArgsArrayS = aiArgs.toArray(new ScriptDataSupplier[0]);
                        }
                    }

                    ScriptData scriptData = scriptAi.equals(this.name) ? this : pthData.getScript(scriptAi);

                    commands.add(runner -> runner.pthData.summonBullet(name, x.get(runner), y.get(runner), scale.get(runner),
                            angle.get(runner), collideType, collideArgs, scriptData, runner, aiArgsArrayS));

                    break;
                }
                case 16: {
                    if (PthData.debug) {
                        System.out.println("kill");
                    }
                    commands.add(runner -> runner.pthData.scriptCallback.add(new Kill()));
                    break;
                }
                case 20: {
                    ScriptDataSetter setter = storeF32(in, scriptFunction);
                    commands.add(setter::set);
                    break;
                }
                case 21: {
                    if (PthData.debug) {
                        System.out.println("+");
                    }
                    commands.add(runner -> runner.pthData.calcStack.add());
                    break;
                }
                case 22: {
                    if (PthData.debug) {
                        System.out.println("-");
                    }
                    commands.add(runner -> runner.pthData.calcStack.sub());
                    break;
                }
                case 23: {
                    if (PthData.debug) {
                        System.out.println("*");
                    }
                    commands.add(runner -> runner.pthData.calcStack.mul());
                    break;
                }
                case 24: {
                    if (PthData.debug) {
                        System.out.println("/");
                    }
                    commands.add(runner -> runner.pthData.calcStack.div());
                    break;
                }
                case 25: {
                    if (PthData.debug) {
                        System.out.println("%");
                    }
                    commands.add(runner -> runner.pthData.calcStack.mod());
                    break;
                }
                case 26: {
                    if (PthData.debug) {
                        System.out.println("==");
                    }
                    commands.add(runner -> runner.pthData.calcStack.eq());
                    break;
                }
                case 27: {
                    if (PthData.debug) {
                        System.out.println("!=");
                    }
                    commands.add(runner -> runner.pthData.calcStack.nq());
                    break;
                }
                case 28: {
                    if (PthData.debug) {
                        System.out.println("<");
                    }
                    commands.add(runner -> runner.pthData.calcStack.lt());
                    break;
                }
                case 29: {
                    if (PthData.debug) {
                        System.out.println(">");
                    }
                    commands.add(runner -> runner.pthData.calcStack.gt());
                    break;
                }
                case 30: {
                    if (PthData.debug) {
                        System.out.println("<=");
                    }
                    commands.add(runner -> runner.pthData.calcStack.le());
                    break;
                }
                case 31: {
                    if (PthData.debug) {
                        System.out.println(">=");
                    }
                    commands.add(runner -> runner.pthData.calcStack.ge());
                    break;
                }
                case 38: {
                    if (PthData.debug) {
                        System.out.println("sin");
                    }
                    ScriptDataSupplier scriptDataSupplier = loadF32(in, scriptFunction);
                    ScriptDataSetter setter = storeF32(in, scriptFunction);
                    commands.add(runner -> {
                        double v = scriptDataSupplier.get(runner);
                        v = Math.sin(v * Math.PI / 180.0);
                        pthData.calcStack.push(v);
                        setter.set(runner);
                    });
                    break;
                }
                case 39: {
                    if (PthData.debug) {
                        System.out.println("cos");
                    }
                    ScriptDataSupplier scriptDataSupplier = loadF32(in, scriptFunction);
                    ScriptDataSetter setter = storeF32(in, scriptFunction);
                    commands.add(runner -> {
                        double v = scriptDataSupplier.get(runner);
                        v = Math.cos(v * Math.PI / 180.0);
                        runner.pthData.calcStack.push(v);
                        setter.set(runner);
                    });
                    break;
                }
                //ret (nothing)
                case 2:
                    //allocate
                case 4: {
                    break;
                }
                default: {
                    throw new IOException("Unknown byte command: " + cur);
                }
            }
        }
        if(PthData.debug) {
            System.out.println("[Function loaded]");
        }
        scriptFunction.commands = commands.toArray(new IScriptCommand[0]);
        scriptFunction.loops = loopMarks.toArray(new Loop[0]);
        return scriptFunction;
    }

    /**
     * get f32 supplier
     *
     * @param in the script file
     * @param f  the function in
     * @return the supplier to get f32 in runtime or null if there is no more data
     * @throws IOException if read file failed
     */
    private ScriptDataSupplier loadF32(DataInputStream in, ScriptFunction f) throws IOException {
        byte from = in.readByte();
        switch (from) {
            case 0: {
                float data = in.readFloat();
                if (PthData.debug) {
                    System.out.println("load f32 from const:" + data);
                }
                return runner -> data;
            }
            case 1: {
                from = in.readByte();
                switch (from) {
                    case 0: {
                        if (PthData.debug) {
                            System.out.println("load f32 from x");
                        }
                        return runner -> runner.x;
                    }
                    case 1: {
                        if (PthData.debug) {
                            System.out.println("load f32 from y");
                        }
                        return runner -> runner.y;
                    }
                    case 3: {
                        if (PthData.debug) {
                            System.out.println("load f32 from px");
                        }
                        return runner -> runner.px;
                    }
                    case 4: {
                        if (PthData.debug) {
                            System.out.println("load f32 from py");
                        }
                        return runner -> runner.py;
                    }
                    case 2:
                    case 5: {
                        //no z in minecraft (
                        if (PthData.debug) {
                            System.out.println("load f32 from z");
                        }
                        return runner -> 0;

                    }
                    default: {
                        throw new IOException("Unknown f32 source (in game value): " + from);
                    }
                }
            }
            case 2: {
                int idx = in.readUnsignedByte();
                if (PthData.debug) {
                    System.out.println("load f32 from data" + idx);
                }
                return runner -> runner.context.data[idx];
            }
            case 3: {
                int idx = in.readUnsignedByte();
                f.maxStack = Math.max(idx, f.maxStack);
                if (PthData.debug) {
                    System.out.println("load f32 from stack" + idx);
                }
                return runner -> runner.context.stack[idx];
            }
            case 4: {
                if (PthData.debug) {
                    System.out.println("load f32 from calc");
                }
                return runner -> runner.pthData.calcStack.pop();
            }
            case 9: {
                if (PthData.debug) {
                    System.out.println("no more f32");
                }
                return null;
            }
            default: {
                throw new IOException("Unknown f32 source: " + from);
            }
        }
    }

    private ScriptDataSetter storeF32(DataInputStream in, ScriptFunction f) throws IOException {
        byte to = in.readByte();
        switch (to) {
            case 1: {
                to = in.readByte();
                switch (to) {
                    case 0: {
                        if (PthData.debug) {
                            System.out.println("calc store to x");
                        }
                        return runner -> runner.x = runner.pthData.calcStack.pop();
                    }
                    case 1: {
                        if (PthData.debug) {
                            System.out.println("calc store to y");
                        }
                        return runner -> runner.y = runner.pthData.calcStack.pop();
                    }
                    case 3: {
                        if (PthData.debug) {
                            System.out.println("calc store to px");
                        }
                        return runner -> runner.px = runner.pthData.calcStack.pop();
                    }
                    case 4: {
                        if (PthData.debug) {
                            System.out.println("calc store to py");
                        }
                        return runner -> runner.py = runner.pthData.calcStack.pop();
                    }
                    case 2:
                    case 5: {
                        //no z in minecraft (
                        if (PthData.debug) {
                            System.out.println("calc store to z");
                        }
                        return runner -> runner.pthData.calcStack.pop();

                    }
                    default: {
                        throw new IOException("Unknown f32 source (in game value): " + to);
                    }
                }
            }
            case 2: {
                int idx = in.readUnsignedByte();
                if (PthData.debug) {
                    System.out.println("calc store to data" + idx);
                }
                return (runner) -> runner.context.data[idx] = runner.pthData.calcStack.pop();
            }
            case 3: {
                int idx = in.readUnsignedByte();
                f.maxStack = Math.max(idx + 1, f.maxStack);
                if (PthData.debug) {
                    System.out.println("calc store to stack" + idx);
                }
                return (runner) -> runner.context.stack[idx] = runner.pthData.calcStack.pop();
            }
            default: {
                throw new IOException("Unknown where to store f32 with: : " + to);
            }
        }
    }

    private IScriptCommand loadF32ToCalcStack(DataInputStream in, ScriptFunction f) throws IOException {

        byte from = in.readByte();
        switch (from) {
            case 0: {
                float data = in.readFloat();
                if (PthData.debug) {
                    System.out.println("push from const: " + data);
                }
                return runner -> runner.pthData.calcStack.push(data);
            }
            case 1: {
                from = in.readByte();
                switch (from) {
                    case 0: {
                        if (PthData.debug) {
                            System.out.println("push from x");
                        }
                        return runner -> runner.pthData.calcStack.push(runner.x);
                    }
                    case 1: {
                        if (PthData.debug) {
                            System.out.println("push from y");
                        }
                        return runner -> runner.pthData.calcStack.push(runner.y);
                    }
                    case 3: {
                        if (PthData.debug) {
                            System.out.println("push from player x");
                        }
                        return runner -> runner.pthData.calcStack.push(runner.px);
                    }
                    case 4: {
                        if (PthData.debug) {
                            System.out.println("push from player y");
                        }
                        return runner -> runner.pthData.calcStack.push(runner.py);
                    }
                    case 2:
                    case 5: {
                        //no z in minecraft (
                        if (PthData.debug) {
                            System.out.println("push from z");
                        }
                        return runner -> runner.pthData.calcStack.push(0);

                    }
                    default: {
                        throw new IOException("Unknown f32 source (in game value): " + from);
                    }
                }
            }
            case 2: {
                int idx = in.readUnsignedByte();
                if (PthData.debug) {
                    System.out.println("push from data" + idx);
                }
                return runner -> runner.pthData.calcStack.push(runner.context.data[idx]);
            }
            case 3: {
                int idx = in.readUnsignedByte();
                f.maxStack = Math.max(idx + 1, f.maxStack);
                if (PthData.debug) {
                    System.out.println("push from stack" + idx);
                }
                return runner -> runner.pthData.calcStack.push(runner.context.stack[idx]);
            }
            default: {
                throw new IOException("Unknown f32 source: " + from);
            }
        }
    }
}
