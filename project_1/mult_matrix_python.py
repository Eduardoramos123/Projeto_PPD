import time

def matrix_mul_1(a, b):

    matrix_out = []
    
    
    for i in range(len(a)):
        line = []
        for j in range(len(b[0])):
            line.append(0)
        matrix_out.append(line)
    
    st = time.time()
    
    for i in range(len(a)):
        for j in range(len(b[0])):
            for k in range(len(b)):
                matrix_out[i][j] += a[i][k] * b[k][j]
            
    
    et = time.time()
    
    elapsed_time = et - st
    print('Execution time:', elapsed_time, 'seconds')
    
    return matrix_out

def matrix_mul_2(a, b):
    matrix_out = []
    
    
    for i in range(len(a)):
        line = []
        for j in range(len(b[0])):
            line.append(0)
        matrix_out.append(line)
    
    st = time.time()
    
    for i in range(len(a)):
        for k in range(len(b)):
            for j in range(len(b[0])):
                matrix_out[i][j] += a[i][k] * b[k][j]
            
    
    et = time.time()
    
    elapsed_time = et - st
    print('Execution time:', elapsed_time, 'seconds')
    
    return matrix_out


while(1):

    print("1. Multiplication")
    print("2. Line Multiplication")
    
    sel = int(input("Selection?:"))
    num = int(input("Numero n de nxn:"))
    
    
    lines = []
    
    matrix1 = []
    matrix2 = []
    
    
    for i in range(num):
        lines.append(1)
        
    
    for i in range(num):
        matrix1.append(lines)
        
        lines2 = []
        for j in range(num):
            lines2.append(i + 1)
        matrix2.append(lines2)
        
    
    if sel == 1:
        res = matrix_mul_1(matrix1, matrix2)            
        print(res[0][0])   
    elif sel == 2:
        res = matrix_mul_2(matrix1, matrix2)            
        print(res[0][0])   
            
        