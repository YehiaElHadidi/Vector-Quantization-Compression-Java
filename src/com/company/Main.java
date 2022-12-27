package com.company;


import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;
import javax.imageio.ImageIO;


class ReadWriteExample {

    //read 2D int pixels from image file
    public int[][] readImage(String filePath) {
        File file = new File(filePath);
        BufferedImage image;
        int width, height;
        try {
            image = ImageIO.read(file);
            width = image.getWidth();
            height = image.getHeight();
            int[][] pixels = new int[height][width];
            int rgb;
            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    rgb = image.getRGB(x, y);
                    pixels[y][x] = (rgb >> 16) & 0xff; // to get red color as our gray scale
//                    pixels[y][x] = rgb & 0xff; // to get blue
                }
            }
            return pixels;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void writeImage(int[][] pixels, String outputFilePath) {
        File fileout = new File(outputFilePath);
        int height = pixels.length;
        int width = pixels[0].length;
        BufferedImage image2 = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                image2.setRGB(x, y, (pixels[y][x] << 16) | (pixels[y][x]) | (pixels[y][x] << 8));
            }
        }
        try {
            ImageIO.write(image2, "png", fileout);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}


public class Main {
    static ReadWriteExample r = new ReadWriteExample();


    public static void main(String[] args) {
        encode();
        decode();
    }

    public static void decode() {
        int og_size = 1;// 6*6 = 36
        int block_size = 1;//2*2 = 4
        int num_of_blocks;//  4
        ArrayList<Matrix> codebook = new ArrayList<>();
        int num_labels;
        int[] labels = new int[0];
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

    public static void encode() {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Enter Image Name (image.png): ");
        String s = scanner.next();
        int[][] arr = r.readImage(s);
        ArrayList<Matrix> vec = new ArrayList<>();
        int wid = arr.length;
        int hig = arr[0].length;
        int codebook;
        System.out.println("Enter the Number of codebooks: ");
        codebook = scanner.nextInt();
        System.out.println("Enter the Size of codebooks: ");
        int divide; // divide*divide element per arr
        divide = scanner.nextInt();


        int blocks = ((wid * hig) / (divide * divide));  // no of blocks
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
        ArrayList<Matrix> average = new ArrayList<>();
        // fst
        {
            int[][] fst = new int[divide][divide];
            int[][] sec = new int[divide][divide];
            for (int i = 0; i < divide; i++) {
                for (int j = 0; j < divide; j++) {
                    fst[i][j] = 0;
                }
            }
            for (int i = 0; i < divide; i++) {
                for (int j = 0; j < divide; j++) {
                    for (Matrix matrix : vec) {
                        fst[i][j] += matrix.matrix[i][j];
                    }
                }
            }
            // split
            for (int i = 0; i < divide; i++) {
                for (int j = 0; j < divide; j++) {
                    fst[i][j] /= vec.size();
                    sec[i][j] = fst[i][j] + 1;
                }
            }
            average.add(new Matrix(fst));
            average.add(new Matrix(sec));
        }

        calculateAvg(average, vec, divide, codebook);

        ArrayList<Matrix> old_avg = new ArrayList<>();
        for (int i = 0; i < average.size(); i++) {
            int[][] n = new int[divide][divide];
            old_avg.add(new Matrix(n));
            for (int j = 0; j < divide; j++) {
                System.arraycopy(average.get(i).matrix[j], 0, old_avg.get(i).matrix[j], 0, divide);
            }
        }
        average.clear();
        ArrayList<Pair> output = stabilizeAvg(average, old_avg, vec, divide, codebook);
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
                if (i % 50 == 0)
                    print.write("\n");
                print.write(output.get(i).codebook + " ");
            }
            print.close();
        } catch (IOException e) {
            System.out.println(1);
        }


    }



    private static int nearest(ArrayList<Matrix> average, int[][] matrix) {
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

    static void calculateAvg(ArrayList<Matrix> average, ArrayList<Matrix> vec, int divide, int codebook) {
        while (true) {
            int[] temp = new int[average.size()];
            int[][][] temp_avg = new int[average.size()][divide][divide];
            // to zero
            initializearrays(divide, average.size(), temp, temp_avg);
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
                if (sz < codebook)
                    average.add(new Matrix(spt));
                average.add(new Matrix(temp_avg[z]));
            }
            if (sz >= codebook) {
                break;
            }
        }
    }

    private static ArrayList<Pair> stabilizeAvg(ArrayList<Matrix> average, ArrayList<Matrix> old_avg, ArrayList<Matrix> vec, int divide, int codebook) {
        int cnt = 0;
        while (true) {
            cnt++;
            int[] temp = new int[old_avg.size()];
            int[][][] temp_avg = new int[old_avg.size()][divide][divide];
            ArrayList<Pair> output = new ArrayList<>();
            initializearrays(divide, old_avg.size(), temp, temp_avg);
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

    private static void initializearrays(int divide, int sz, int[] temp, int[][][] temp_avg) {
        for (int i = 0; i < sz; i++) {
            temp[i] = 0;
            for (int j = 0; j < divide; j++) {
                for (int k = 0; k < divide; k++) {
                    temp_avg[i][j][k] = 0;
                }
            }
        }
    }


}
