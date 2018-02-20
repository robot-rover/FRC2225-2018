package org.usfirst.frc.team2225.season2018.jetson;

import jcuda.Pointer;
import jcuda.driver.CUfunction;
import jcuda.driver.CUmodule;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

import static jcuda.driver.JCudaDriver.*;

public class Shader {
    public static File shaderDir = new File("kernelSource/");
    public static File kernelDir = new File("kernel/");
    static {
        shaderDir.mkdir();
        kernelDir.mkdir();
    }

    public InputStream source;
    public File sourceFile;
    public String name;
    public Pointer params;

    public CUfunction function;
    public Shader(String shaderName, boolean recompile) throws IOException {
        source = ClassLoader.getSystemResourceAsStream("cuda/" + shaderName + ".cu");
        sourceFile = new File(shaderDir,shaderName + ".cu");
        name = shaderName;
        CUmodule module = new CUmodule();

        cuModuleLoad(module, preparePtxFile(recompile).getPath());

        // Obtain a function pointer to the "sampleKernel" function.
        function = new CUfunction();
        cuModuleGetFunction(function, module, name);
    }

    public Shader(String shaderName) throws IOException {
        this(shaderName,false);
    }

    private File preparePtxFile(boolean recompile) throws IOException
    {
        if (!sourceFile.exists() || recompile)
        {
            Files.copy(source, sourceFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
        }
        File ptxFile = new File(kernelDir, name + ".ptx");
        if(!ptxFile.exists() || recompile) {
            String modelString = "-m" + System.getProperty("sun.arch.data.model");
            String command =
                    "nvcc " + modelString + " -ptx " +
                            sourceFile.getPath() + " -o " + ptxFile.getPath();

            System.out.println("Executing\n" + command);
            Process process = Runtime.getRuntime().exec(command);

            String errorMessage =
                    new String(toByteArray(process.getErrorStream()));
            int exitValue = 0;
            try {
                exitValue = process.waitFor();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new IOException(
                        "Interrupted while waiting for nvcc output", e);
            }

            if (exitValue != 0) {
                throw new IOException(
                        "Could not create .ptx file: " + errorMessage);
            }

            System.out.println("Finished creating PTX file");
        }
        return ptxFile;
    }

    private static byte[] toByteArray(InputStream inputStream)
            throws IOException
    {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte buffer[] = new byte[8192];
        while (true)
        {
            int read = inputStream.read(buffer);
            if (read == -1)
            {
                break;
            }
            baos.write(buffer, 0, read);
        }
        return baos.toByteArray();
    }

}
