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
                ScriptFunction function = loadFunction(pthData, in);
                functions.put(funName, function);
            }
            tickFunction = functions.remove("tick");
        }
    }

    @SuppressWarnings("ConstantConditions for trust script compiler.")
    private ScriptFunction loadFunction(PthData pthData, DataInputStream in) throws IOException {
        byte cur;
        ScriptFunction scriptFunction = new ScriptFunction();
        scriptFunction.maxStack = 0;
        LinkedList<IScriptCommand> commands = new LinkedList<>();
        LinkedList<Loop> loopMarks = new LinkedList<>();
        int loops = 0;
        loop:
        while (true) {
            cur = in.readByte();
            switch (cur) {
                case 0: {
                    if (loops > 0) {
                        commands.add(runner -> {
                            for (int i = runner.function.loops.length - 1; i >= 0; i--) {
                                Loop loop = runner.function.loops[i];
                                if (loop.pointer < runner.context.pointer) {
                                    if (loop.type == Loop.Type.START) {
                                        runner.context.pointer = loop.pointer;
                                        return;
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
                    loops += 1;
                    loopMarks.add(new Loop(Loop.Type.START, commands.size() - 1));
                    break;
                }
                case 3: {
                    commands.add(loadF32ToCalcStack(in, scriptFunction));
                    break;
                }
                case 5: {
                    ScriptDataSupplier b = loadF32(in, scriptFunction);
                    commands.add(runner -> {
                        double value = b.get(runner);
                        if (value > 1.0) {
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
                    ScriptDataSupplier b = loadF32(in, scriptFunction);
                    commands.add(runner -> runner.context.wait = (int) Math.floor(b.get(runner)));
                    break;
                }
                case 10: {
                    ScriptDataSupplier b = loadF32(in, scriptFunction);
                    commands.add(runner -> runner.pthData.scriptCallback.add(new MoveUp(b.get(runner))));
                    break;
                }
                case 11: {
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
                    commands.add(runner -> runner.pthData.scriptCallback.add(new Kill()));
                    break;
                }
                case 20: {
                    storeF32(in, scriptFunction);
                    break;
                }
                case 21: {
                    commands.add(runner -> runner.pthData.calcStack.add());
                    break;
                }
                case 22: {
                    commands.add(runner -> runner.pthData.calcStack.sub());
                    break;
                }
                case 23: {
                    commands.add(runner -> runner.pthData.calcStack.mul());
                    break;
                }
                case 24: {
                    commands.add(runner -> runner.pthData.calcStack.div());
                    break;
                }
                case 25: {
                    commands.add(runner -> runner.pthData.calcStack.mod());
                    break;
                }
                case 26: {
                    commands.add(runner -> runner.pthData.calcStack.eq());
                    break;
                }
                case 27: {
                    commands.add(runner -> runner.pthData.calcStack.nq());
                    break;
                }
                case 28: {
                    commands.add(runner -> runner.pthData.calcStack.lt());
                    break;
                }
                case 29: {
                    commands.add(runner -> runner.pthData.calcStack.gt());
                    break;
                }
                case 30: {
                    commands.add(runner -> runner.pthData.calcStack.le());
                    break;
                }
                case 31: {
                    commands.add(runner -> runner.pthData.calcStack.ge());
                    break;
                }
                case 38: {
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
                    ScriptDataSupplier scriptDataSupplier = loadF32(in, scriptFunction);
                    ScriptDataSetter setter = storeF32(in, scriptFunction);
                    commands.add(runner -> {
                        double v = scriptDataSupplier.get(runner);
                        v = Math.cos(v * Math.PI / 180.0);
                        pthData.calcStack.push(v);
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
                return runner -> data;
            }
            case 1: {
                from = in.readByte();
                switch (from) {
                    case 0: {
                        return runner -> runner.x;
                    }
                    case 1: {
                        return runner -> runner.y;

                    }
                    case 3: {
                        return runner -> runner.px;
                    }
                    case 4: {
                        return runner -> runner.py;
                    }
                    case 2:
                    case 5: {
                        //no z in minecraft (
                        return runner -> 0;

                    }
                    default: {
                        throw new IOException("Unknown f32 source (in game value): " + from);
                    }
                }
            }
            case 2: {
                int idx = in.readUnsignedByte();
                return runner -> runner.context.data[idx];
            }
            case 3: {
                int idx = in.readUnsignedByte();
                f.maxStack = Math.max(idx, f.maxStack);
                return runner -> runner.context.stack[idx];
            }
            case 4: {
                return runner -> runner.pthData.calcStack.pop();
            }
            case 9: {
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
                        return runner -> runner.x = runner.pthData.calcStack.pop();
                    }
                    case 1: {
                        return runner -> runner.y = runner.pthData.calcStack.pop();


                    }
                    case 3: {
                        return runner -> runner.px = runner.pthData.calcStack.pop();

                    }
                    case 4: {
                        return runner -> runner.py = runner.pthData.calcStack.pop();

                    }
                    case 2:
                    case 5: {
                        //no z in minecraft (
                        return runner -> {
                        };

                    }
                    default: {
                        throw new IOException("Unknown f32 source (in game value): " + to);
                    }
                }
            }
            case 2: {
                int idx = in.readUnsignedByte();
                return (runner) -> runner.context.data[idx] = runner.pthData.calcStack.pop();
            }
            case 3: {
                int idx = in.readUnsignedByte();
                f.maxStack = Math.max(idx + 1, f.maxStack);
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
                return runner -> runner.pthData.calcStack.push(data);
            }
            case 1: {
                from = in.readByte();
                switch (from) {
                    case 0: {
                        return runner -> runner.pthData.calcStack.push(runner.x);
                    }
                    case 1: {
                        return runner -> runner.pthData.calcStack.push(runner.y);

                    }
                    case 3: {
                        return runner -> runner.pthData.calcStack.push(runner.px);
                    }
                    case 4: {
                        return runner -> runner.pthData.calcStack.push(runner.py);
                    }
                    case 2:
                    case 5: {
                        //no z in minecraft (
                        return runner -> runner.pthData.calcStack.push(0);

                    }
                    default: {
                        throw new IOException("Unknown f32 source (in game value): " + from);
                    }
                }
            }
            case 2: {
                int idx = in.readUnsignedByte();
                return runner -> runner.pthData.calcStack.push(runner.context.data[idx]);
            }
            case 3: {
                int idx = in.readUnsignedByte();
                f.maxStack = Math.max(idx + 1, f.maxStack);
                return runner -> runner.pthData.calcStack.push(runner.context.stack[idx]);
            }
            default: {
                throw new IOException("Unknown f32 source: " + from);
            }
        }
    }
}
