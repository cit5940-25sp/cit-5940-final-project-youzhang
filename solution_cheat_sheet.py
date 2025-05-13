import pandas as pd
import ast
from collections import defaultdict, deque

def find_separate_paths(csv_path, output_path):
    df = pd.read_csv(csv_path)
    df['cast'] = df['cast'].apply(ast.literal_eval)
    df['crew'] = df['crew'].apply(ast.literal_eval)

    cast_to_movies = defaultdict(set)
    crew_to_movies = defaultdict(set)
    title_to_cast = {}
    title_to_crew = {}

    for _, row in df.iterrows():
        title = row['title']
        cast_ids = set(pid for _, pid in row['cast'])
        crew_ids = set(pid for _, pid in row['crew'])

        title_to_cast[title] = cast_ids
        title_to_crew[title] = crew_ids

        for pid in cast_ids:
            cast_to_movies[pid].add(title)
        for pid in crew_ids:
            crew_to_movies[pid].add(title)

    def build_graph(mapping, title_to_people):
        graph = defaultdict(set)
        for title, people in title_to_people.items():
            neighbors = set()
            for pid in people:
                neighbors |= mapping[pid]
            neighbors.discard(title)
            graph[title] = neighbors
        return graph

    cast_graph = build_graph(cast_to_movies, title_to_cast)
    crew_graph = build_graph(crew_to_movies, title_to_crew)

    def bfs(graph, start):
        visited = set()
        queue = deque([(start, [start])])
        while queue:
            current, path = queue.popleft()
            if len(path) == 4:
                return path[1:]  # 不包含起点
            for neighbor in graph[current]:
                if neighbor not in path:
                    queue.append((neighbor, path + [neighbor]))
        return []

    rows = []
    for title in df['title']:
        cast_path = bfs(cast_graph, title)
        crew_path = bfs(crew_graph, title)
        rows.append({
            'movie title': title,
            'cast_path': cast_path,
            'crew_path': crew_path
        })

    pd.DataFrame(rows).to_csv(output_path, index=False)

# find_separate_paths('data/movies.csv', 'data/movie_paths.csv')