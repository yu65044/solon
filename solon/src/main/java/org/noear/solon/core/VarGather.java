package org.noear.solon.core;

import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * 变量收集器（主要为 XConfiguration 构建 method bean 时服务）
 *
 * @author noear
 * @since 1.0
 * */
public class VarGather implements Runnable {
    //变量
    List<VarHolderOfParam> vars;
    //完成时
    Consumer<Object[]> done;

    public VarGather(int varSize, Consumer<Object[]> done) {
        this.done = done;
        this.vars = new ArrayList<>(varSize);
    }

    public VarHolder add(Parameter p) {
        VarHolderOfParam p2 = new VarHolderOfParam(p, this);
        vars.add(p2);
        return p2;
    }

    @Override
    public void run() {
        for(VarHolderOfParam p1 : vars){
            if(p1.isDone() == false){
                return;
            }
        }

        List<Object> args = new ArrayList<>(vars.size());
        for(VarHolderOfParam p1 : vars){
            args.add(p1.getValue());
        }

        done.accept(args.toArray());
    }
}
