package com.company;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;

public class Encode {
    ReadWriteExample r = new ReadWriteExample();
    int divide; // divide*divide element per arr
    ArrayList<Matrix> vec = new ArrayList<>();
    int codebook;
    int wid;
    int blocks;
    int[][] arr;
    int hig;

    public void run() {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Enter Image Name (image.png): ");
        String s = scanner.next();
        arr = r.readImage(s);
        wid = arr.length;
        hig = arr[0].length;
        System.out.println("Enter the Number of codebooks: ");
        codebook = scanner.nextInt();
        System.out.println("Enter the Size of codebooks: ");
        divide = scanner.nextInt();

        quantitze();

        ArrayList<Matrix> average = new ArrayList<>();
        average.add(new Matrix(new int[divide][divide]));

        calculateAvg(average);

        ArrayList<Matrix> old_avg = new ArrayList<>();
        for (int i = 0; i < average.size(); i++) {
            int[][] n = new int[divide][divide];
            old_avg.add(new Matrix(n));
            for (int j = 0; j < divide; j++) {
                System.arraycopy(average.get(i).matrix[j], 0, old_avg.get(i).matrix[j], 0, divide);
            }
        }
        average.clear();

        ArrayList<Pair> output = stabilizeAvg(average, old_avg);

        writeToFile(output, average);
    }

    private void quantitze() {
        blocks = ((wid * hig) / (divide * divide));  // no of blocks
        for (int z = 0; z < Math.sqrt(blocks); z++) {
            for (int p = 0; p < Math.sqrt(blocks); p++) {
                int[][] qnt = new int[divide][divide];
                for (int i = z * divide; i < z * divide + divide; i++) {
                    for (int j = p * divide; j < p * divide + divide; j++) {
                        qnt[i % divide][j % divide] = arr[i][j];
                    }
                }
                vec.add(new Matrix(qnt));
            }
        }
    }

    private int nearest(ArrayList<Matrix> average, int[][] matrix) {
        int mn = 9999999, ind = 0;
        for (int z = 0; z < average.size(); z++) {
            int near = 0;
            for (int i = 0; i < average.get(z).dimension; i++) {
                for (int j = 0; j < average.get(z).dimension; j++) {
                    near += Math.abs(average.get(z).matrix[i][j] - matrix[i][j]);
                }
            }
            if (near < mn) {
                mn = near;
                ind = z;
            }
        }
        return ind;
    }

    void calculateAvg(ArrayList<Matrix> average) {
        while (true) {
            int[] temp = new int[average.size()];
            int[][][] temp_avg = new int[average.size()][divide][divide];
            // to zero
            for (Matrix matrix : vec) {
                int ind = nearest(average, matrix.matrix);
                for (int i = 0; i < divide; i++) {
                    for (int j = 0; j < divide; j++) {
                        temp_avg[ind][i][j] += matrix.matrix[i][j];
                    }
                }
                temp[ind]++;
            }
            System.out.println(average.size());
            int sz = average.size();
            average.clear();
            for (int z = 0; z < sz; z++) {
                int[][] spt = new int[divide][divide];
                for (int i = 0; i < divide; i++) {
                    for (int j = 0; j < divide; j++) {
                        temp_avg[z][i][j] /= temp[z];
                        spt[i][j] = temp_avg[z][i][j] + 1;
                    }
                }
                if (sz < codebook) average.add(new Matrix(spt));
                average.add(new Matrix(temp_avg[z]));
            }
            if (sz >= codebook) {
                break;
            }
        }
    }

    private ArrayList<Pair> stabilizeAvg(ArrayList<Matrix> average, ArrayList<Matrix> old_avg) {
        int cnt = 0;
        while (true) {
            cnt++;
            int[] temp = new int[old_avg.size()];
            int[][][] temp_avg = new int[old_avg.size()][divide][divide];
            ArrayList<Pair> output = new ArrayList<>();
            for (int k = 0; k < vec.size(); k++) {
                int ind = nearest(old_avg, vec.get(k).matrix);
                for (int i = 0; i < divide; i++) {
                    for (int j = 0; j < divide; j++) {
                        temp_avg[ind][i][j] += vec.get(k).matrix[i][j];
                    }
                }
                output.add(new Pair(k, ind));
                temp[ind]++;
            }
            for (int z = 0; z < codebook; z++) {
                for (int i = 0; i < divide; i++) {
                    for (int j = 0; j < divide; j++) {
                        temp_avg[z][i][j] /= temp[z];
                    }
                }
                average.add(new Matrix(temp_avg[z]));
            }
            boolean flag = false;
            for (int z = 0; z < codebook; z++) {
                for (int i = 0; i < divide; i++) {
                    for (int j = 0; j < divide; j++) {
                        if (!(old_avg.get(z).matrix[i][j] == average.get(z).matrix[i][j])) {
                            flag = true;
                            break;
                        }
                    }
                    if (flag) break;
                }
                if (flag) break;
            }
            // average may not stabilize
            if (flag && cnt < 10) {
                for (int i = 0; i < average.size(); i++) {
                    for (int j = 0; j < divide; j++) {
                        System.arraycopy(average.get(i).matrix[j], 0, old_avg.get(i).matrix[j], 0, divide);
                    }
                }
                average.clear();
            } else return output;
        }
    }

    private void writeToFile(ArrayList<Pair> output, ArrayList<Matrix> average) {
        try {
            FileWriter print = new FileWriter("encoded.txt");
            print.write(wid + "\n");
            print.write(divide + "\n");
            print.write(average.size() + "\n");
            for (Matrix matrix : average) {
                print.write("\n");
                for (int j = 0; j < divide; j++) {
                    for (int k = 0; k < divide; k++) {
                        print.write(matrix.matrix[j][k] + " ");
                    }
                    print.write("\n");
                }
            }
            print.write("\n");
            for (int i = 0; i < output.size(); i++) {
                if (i % 50 == 0) print.write("\n");
                print.write(output.get(i).codebook + " ");
            }
            print.close();
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }
}
