package com.company;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;

public class Decode {
     ReadWriteExample r = new ReadWriteExample();
    Integer og_size = 1;// 6*6 = 36
    Integer block_size = 1;//2*2 = 4
    Integer num_of_blocks = 1;//  4
    ArrayList<Matrix> codebook = new ArrayList<>();
    Integer num_labels = 1;
    int[] labels;


    public void run() {
        readFromFile();

        Matrix decompressed = new Matrix(og_size);
        int coffx = 0, coffy = 0;
        for (int label : labels) {
            Matrix temp = codebook.get(label);
            for (int tempi = 0, i = coffx; i < coffx + block_size; i++, tempi++) {
                for (int tempj = 0, j = coffy; j < coffy + block_size; j++, tempj++) {
                    decompressed.matrix[i][j] = temp.matrix[tempi][tempj];
                }
            }
            coffy += block_size;
            if (coffy % og_size == 0) {
                coffy = 0;
                coffx += block_size;
            }
        }
        System.out.println("-------- CHECK print.png ---------");
        r.writeImage(decompressed.matrix, "print.png");
    }

    private void readFromFile() {
        try {
            File read = new File("encoded.txt");
            Scanner fileRead = new Scanner(read);
            og_size = fileRead.nextInt();
            block_size = fileRead.nextInt();
            num_of_blocks = fileRead.nextInt();
            num_labels = (og_size * og_size) / (block_size * block_size);
            labels = new int[num_labels];
            for (int i = 0; i < num_of_blocks; i++) {
                int[] mat = new int[block_size * block_size];
                for (int j = 0; j < block_size * block_size; j++) {
                    mat[j] = fileRead.nextInt(); // enters 2*2 matrix in form of array
                }
                Matrix temp = new Matrix(block_size);
                temp.create(mat); // converts array into matrix
                codebook.add(temp);// adds to codebook
            }
            for (int i = 0; i < num_labels; i++)
                labels[i] = fileRead.nextInt();
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }
}
