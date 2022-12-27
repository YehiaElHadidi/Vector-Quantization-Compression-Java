package com.company;

public class Matrix {
    int[][] matrix;
    int dimension;

    public Matrix(int d){
        matrix  = new int[d][d];
        dimension = d;
    }

    Matrix(int mat[][]){
        matrix = mat;
        dimension = mat.length;
    }

    public void create(int[] mat){
        int idx = 0;
        for(int i=0;i<dimension;i++){
            for(int j=0;j<dimension;j++){
                matrix[i][j] = mat[idx];
                idx++;
            }
        }
    }
    public void display(){
        for(int i=0;i<dimension;i++){
            for(int j=0;j<dimension;j++){
                System.out.print(matrix[i][j] + " ");
            }
            System.out.println();
        }
    }
}
