package com.company;


import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;
import javax.imageio.ImageIO;


public class Main {


    public static void main(String[] args) {
        new Encode().run();
        new Decode().run();
    }


}
