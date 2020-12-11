# Lab 5 - Memory optimization
## Initial input file and its analysis

```
int[][][] a = new int[100][100][100];

for (int i = 0; i < 100; i++) {
        for (int j = 0; j < 100; j++) {
                for (int k = 0; k < 100; k++) {
                    a[k][j][i]++;
                }
        }
}
```

The code seems rather primitive and easy. We are creating triple array and
by default it is initialized by zeros. And then with help of triple cycles
we increment all elements of triple array.

At first glance, in this case there is nothing to optimize. The easiest and 
best way to walk through the elements of the triple array is three cycles. This is true, 
but there is actually a difference in the way we go through the triple array.

## Optimized code and its analysis

Consider such a property as the locality of the data. When we refer to different data, 
it is desirable that they be next to each other in memory.

In the example above, we cover the triple array on the third array, this is a bad practice,
 because in reality the data of the triple array is stored on the first array. That is, we 
 first turned to the first element of every third array, then to another, and so on. Let's optimize this a bit:
 
```
int[][][] a = new int[100][100][100];

for (int i = 0; i < 100; i++) {
        for (int j = 0; j < 100; j++) {
                for (int k = 0; k < 100; k++) {
                    a[i][j][k]++;
                }
        }
}
```

Now we turn in turn to all elements of the first element of the first array, 
then to all elements of the second element of the first array, then to the third and so on ...

Did it really give us any gain?

## Time measurements of both examples

To compare the running time, I ran each of the options 10 times and calculated the avg of each

```
long sum = 0;
for (int i = 0; i < 10; i++) {
    sum += getTimeOriginalFunction();
}
System.out.println(sum / 10);

sum = 0;
for (int i = 0; i < 10; i++) {
    sum += getTimeOptimizedFunction();
}
System.out.println(sum / 10);
```

The result of the first (non-optimized): **11240259** nanoseconds.

The result of the second (optimized): **5716740** nanoseconds.

As we can see, we have a gain in time. At first glance, this is a small amount, but in large
 programs that work with a lot of big data, it can significantly reduce execution time.