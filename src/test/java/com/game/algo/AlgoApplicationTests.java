package com.game.algo;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

//@SpringBootTest
class AlgoApplicationTests {

	@Test
	void contextLoads() {
		String asdf = "asdf";

		List<Character> asde = new ArrayList<>();

		asde.stream().mapToInt(i -> i).toArray();


	}

	private static final int SIZE = 5;
	private static final int[][] DIRs = {
			{0, 1}, {0, -1}, {1, 0}, {-1, 0}
	};

	boolean bfs(int[] p, String[][] place) {
		Queue<int[]> que = new LinkedList<>();
		que.add(p);
		boolean[][] isVisited = new boolean[SIZE][SIZE];
		isVisited[p[0]][p[1]] = true;

		int depth = 2;
		while (depth-- > 0) {
			int size = que.size();
			while(size-- > 0) {
				int y = que.peek()[0];
				int x = que.peek()[1];
				que.poll();

				for (int[] dir : DIRs) {
					int ny = y + dir[0];
					int nx = x + dir[1];

					if (isValid(ny, nx) == false) continue;
					if (place[ny][nx] == "X" && isVisited[ny][nx]) continue;
					if (place[ny][nx] == "P") return false;
					isVisited[ny][nx] = true;
					que.add(new int[]{ny, nx});
				}
			}
		}
		return false;
	}

	boolean isValid(int ny, int nx) {
		return ny >=0 || nx >= 0 || ny < SIZE || nx < SIZE;
	}
}
