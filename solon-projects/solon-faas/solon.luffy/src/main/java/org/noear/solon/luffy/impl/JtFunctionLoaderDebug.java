package org.noear.solon.luffy.impl;

import org.noear.luffy.model.AFileModel;
import org.noear.solon.Solon;
import org.noear.solon.core.util.IoUtil;
import org.noear.solon.core.util.ResourceUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;

/**
 * 函数加载器 - 资源目录实现(调试模式)
 *
 * @author noear
 * @since 1.3
 */
public class JtFunctionLoaderDebug implements JtFunctionLoader {
    static final Logger log = LoggerFactory.getLogger(JtFunctionLoaderDebug.class);

    private String _baseUri = "/luffy/";
    private File _baseDir;

    public JtFunctionLoaderDebug() {
        String rootdir = ResourceUtil.getResource("/").toString()
                .replace("target/classes/", "")
                .replace("target/test-classes/", "");

        if (rootdir.startsWith("file:")) {
            String dir_str = rootdir + "src/main/resources" + _baseUri;
            _baseDir = new File(URI.create(dir_str));
            if (!_baseDir.exists()) {
                dir_str = rootdir + "src/main/webapp" + _baseUri;
                _baseDir = new File(URI.create(dir_str));
            }
        }
    }

    @Override
    public AFileModel fileGet(String path) throws Exception {
        AFileModel file = new AFileModel();

        file.content = fileContentGet(path);

        if (file.content != null) {
            //如果有找到文件内容，则完善信息
            //
            File file1 = new File(path);
            String fileName = file1.getName();

            file.path = path;
            file.tag = "luffy";

            if (fileName.indexOf('.') > 0) {
                String suffix = fileName.substring(fileName.indexOf('.') + 1);
                file.edit_mode = JtMapping.getActuator(suffix);
            } else {
                file.edit_mode = JtMapping.getActuator("");
            }

            //添加id
            file.file_id = path.hashCode();
        }

        return file;
    }

    protected String fileContentGet(String path) {
        if (_baseDir == null) {
            return null;
        } else {
            File file = new File(_baseDir, path);

            if (file.exists() && file.isFile()) {
                try {
                    try (InputStream ins = new FileInputStream(file)) {
                        return IoUtil.transferToString(ins, Solon.encoding());
                    }
                } catch (IOException e) {
                    log.warn(e.getMessage(), e);
                    return null;
                }
            } else {
                return null;
            }
        }
    }
}
