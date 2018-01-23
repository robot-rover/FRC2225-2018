package org.usfirst.frc.team2225.season2018.jetson;

import org.jocl.cl_context;
import org.jocl.cl_kernel;
import org.jocl.cl_program;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.stream.Collectors;

import static org.jocl.CL.*;

public class Shader {
    public static String shaderDir = "jetson/src/main/openclc/";
    String source;

    public cl_program program;
    public cl_kernel kernel;
    public Shader(String shaderName, cl_context context) {
        try {
            source = Files.readAllLines(Paths.get(shaderDir).resolve(shaderName + ".cl")).stream().collect(Collectors.joining("\n"));
        } catch (IOException e) {
            e.printStackTrace();
            source = "";
        }

        // Create the program from the source code
        program = clCreateProgramWithSource(context,
                1, new String[]{source}, null, null);

        // Build the program
        clBuildProgram(program, 0, null, null, null, null);

        // Create the kernel
        kernel = clCreateKernel(program, shaderName, null);
    }

    public void release() {
        clReleaseKernel(kernel);
        clReleaseProgram(program);
    }
}
