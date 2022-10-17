import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class MovieAnalyzer {

  static Supplier<Stream<Movie>> sentence;

  public MovieAnalyzer(String dataset_path) {
    List<Movie> movies = new ArrayList<>();
    try (
        InputStreamReader fReader = new InputStreamReader(new FileInputStream(dataset_path),
            StandardCharsets.UTF_8);
        BufferedReader infile = new BufferedReader(fReader)) {
      String line;
      String[] parts;
      int cnt = 0;
      while ((line = infile.readLine()) != null) {
        parts = line.split(",(?=([^\"]*\"[^\"]*\")*[^\"]*$)");
        if (parts.length <= 16) {
          if (cnt != 0) {
            Movie m = new Movie();
            m.title = parts[1];
            if (parts[2].equals("")) {
              m.year = null;
            } else {
              m.year = Integer.parseInt(parts[2]);
            }
            m.certificate = parts[3];
            String runtime = parts[4].replace("min", "");
            runtime = runtime.trim();
            m.runtime = Integer.parseInt(runtime);
            m.genre = parts[5];
            if (parts[6].equals("")) {
              m.rating = null;
            } else {
              m.rating = Float.parseFloat(parts[6]);
            }
            m.overview = parts[7];
            if (parts[8].equals("")) {
              m.score = null;
            } else {
              m.score = Integer.parseInt(parts[8]);
            }
            m.director = parts[9];
            m.star1 = parts[10];
            m.star2 = parts[11];
            m.star3 = parts[12];
            m.star4 = parts[13];
            if (parts[14].equals("")) {
              m.vote = null;
            } else {
              m.vote = Integer.parseInt(parts[14]);
            }
            if (parts.length == 15 || parts[15].equals("")) {
              m.gross = -1;
            } else {
              String gross_s = parts[15].replace("\"", "");
              gross_s = gross_s.replace(",", "");
              m.gross = Integer.parseInt(gross_s);
            }
            movies.add(m);
          }
          cnt++;
        }
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
    sentence = movies::stream;
  }

  public Map<Integer, Integer> getMovieCountByYear() {
    Map<Integer, Integer> resultMap = new LinkedHashMap<>();
    Map<Integer, Long> collectMap = sentence.get()
        .collect(Collectors.groupingBy(Movie::getYear, Collectors.counting()));
    collectMap.entrySet()
        .stream().sorted((v1, v2) -> {
          if (v1.getKey().equals(v2.getKey())) {
            return v2.getValue().compareTo(v1.getValue());
          } else {
            return v2.getKey().compareTo(v1.getKey());
          }
        }).forEachOrdered(e -> resultMap.put(e.getKey(), e.getValue().intValue()));
    return resultMap;
  }

  public Map<String, Integer> getMovieCountByGenre() {
    Map<String, Integer> resultMap = new LinkedHashMap<>();
    Map<String, Integer> collectMap = new LinkedHashMap<>();
    sentence.get().forEach(item -> {
      String genre = item.getGenre();
      String[] gList = genre.split(",");
      for (String g : gList) {
        g = g.replace("\"", "");
        g = g.replace(" ", "");
        if (!collectMap.containsKey(g)) {
          collectMap.put(g, 1);
        } else {
          collectMap.put(g, collectMap.get(g) + 1);
        }
      }
    });
    collectMap.entrySet()
        .stream().sorted((v1, v2) -> {
          if (v1.getValue().equals(v2.getValue())) {
            return v1.getKey().compareTo(v2.getKey());
          } else {
            return v2.getValue().compareTo(v1.getValue());
          }
        }).forEachOrdered(x -> resultMap.put(x.getKey(), x.getValue()));
    return resultMap;

  }

  public Map<List<String>, Integer> getCoStarCount() {
    Map<List<String>, Integer> resultMap = new LinkedHashMap<>();
    Map<List<String>, Integer> collectMap = new LinkedHashMap<>();
    sentence.get().forEach(item -> {
      String[] star = new String[]{item.getStar1(), item.getStar2(), item.getStar3(),
          item.getStar4()};
      for (int i = 0; i < 4; i++) {
        for (int j = i + 1; j < 4; j++) {
          List<String> starList = new ArrayList<>();
          starList.add(star[i]);
          starList.add(star[j]);
          starList = starList.stream().sorted().collect(Collectors.toList());
          if (!collectMap.containsKey(starList)) {
            collectMap.put(starList, 1);
          } else {
            collectMap.put(starList, collectMap.get(starList) + 1);
          }
        }

      }

    });
    resultMap = collectMap;

    return resultMap;
  }

  public List<String> getTopMovies(int top_k, String by) {
    List<String> resultList = new ArrayList<>();
    if (by.equals("runtime")) {
      List<Movie> collectList = sentence.get().sorted((v1, v2) -> {
        if (Objects.equals(v1.getRuntime(), v2.getRuntime())) {
          return v1.getTitle().replaceAll("^\"*|\"*$", "")
              .compareTo(v2.getTitle().replaceAll("^\"*|\"*$", ""));
        } else {
          return v2.getRuntime().compareTo(v1.getRuntime());
        }
      }).collect(Collectors.toList());
      for (int i = 0; i < top_k; i++) {
        resultList.add(collectList.get(i).getTitle().replaceAll("^\"*|\"*$", ""));
      }
    }
    if (by.equals("overview")) {
      Comparator<Movie> compByLength = Comparator.comparing(amovie -> amovie.getOverview()
          .replaceAll("^\"*|\"*$", "").length(), Comparator.reverseOrder());
      List<Movie> collectList = sentence.get().sorted(compByLength
              .thenComparing(item -> item.title.replaceAll("^\"*|\"*$", "")))
          .collect(Collectors.toList());
      for (int i = 0; i < top_k; i++) {
        resultList.add(collectList.get(i).getTitle().replace("\"", ""));
      }
    }

    return resultList;
  }

  public List<String> getTopStars(int top_k, String by) {
    List<String> resultList = new ArrayList<>();
    if (by.equals("rating")) {
      Map<String, Double> ratingMap = new HashMap<>();
      Map<String, Integer> timesMap = new HashMap<>();
      sentence.get().forEach(item -> {
        String[] star = new String[]{item.getStar1(), item.getStar2(), item.getStar3(),
            item.getStar4()};
        float rate = item.getRating();
        for (int i = 0; i < 4; i++) {
          if (!timesMap.containsKey(star[i])) {
            ratingMap.put(star[i], (double) rate);
            timesMap.put(star[i], 1);
          } else {
            ratingMap.put(star[i], ratingMap.get(star[i]) + rate);
            timesMap.put(star[i], timesMap.get(star[i]) + 1);
          }
        }

      });
      for (Map.Entry<String, Double> entry : ratingMap.entrySet()) {
        String mapKey = entry.getKey();
        Double mapValue = entry.getValue();
        Double nmapValue = mapValue / timesMap.get(mapKey);
        ratingMap.put(mapKey, nmapValue);
      }
      List<Map.Entry<String, Double>> mapList = new ArrayList<>(ratingMap.entrySet());
      mapList.sort((o1, o2) -> {
        if (o1.getValue() > o2.getValue()) {
          return -1;
        } else if (o1.getValue() < o2.getValue()) {
          return 1;
        } else {
          return Integer.compare(0, o2.getKey().compareTo(o1.getKey()));
        }
      });
      int cnt = 0;
      for (Map.Entry<String, Double> entry : mapList) {
        resultList.add(entry.getKey());
        cnt++;
          if (cnt == top_k) {
              break;
          }
      }
      return resultList;
    }
    if (by.equals("gross")) {
      Map<String, Long> grossMap = new HashMap<>();
      Map<String, Integer> timesMap = new HashMap<>();
      sentence.get().forEach(item -> {
        String[] star = new String[]{item.getStar1(), item.getStar2(), item.getStar3(),
            item.getStar4()};
        int gross = 0;
        if (item.getGross() != -1) {
          gross = item.getGross();
          for (int i = 0; i < 4; i++) {
            if (!timesMap.containsKey(star[i])) {
              grossMap.put(star[i], (long) gross);
              timesMap.put(star[i], 1);
            } else {
              grossMap.put(star[i], grossMap.get(star[i]) + gross);
              timesMap.put(star[i], timesMap.get(star[i]) + 1);
            }
          }
        }
      });
      for (Map.Entry<String, Long> entry : grossMap.entrySet()) {
        String mapKey = entry.getKey();
        Long mapValue = entry.getValue();
        Long nmapValue = mapValue / timesMap.get(mapKey);
        grossMap.put(mapKey, nmapValue);
      }
      List<Map.Entry<String, Long>> mapList = new ArrayList<>(grossMap.entrySet());
      mapList.sort((o1, o2) -> {
        if (o1.getValue() > o2.getValue()) {
          return -1;
        } else if (o1.getValue() < o2.getValue()) {
          return 1;
        } else {
          return Integer.compare(0, o2.getKey().compareTo(o1.getKey()));
        }
      });
      int cnt = 0;
      for (Map.Entry<String, Long> entry : mapList) {
        resultList.add(entry.getKey());
        cnt++;
          if (cnt == top_k) {
              break;
          }
      }
      return resultList;
    }
    return resultList;
  }

  public List<String> searchMovies(String genre, float min_rating, int max_runtime) {
    List<String> resultList = new ArrayList<>();
    List<Movie> movieList = sentence.get().filter(m -> m.getGenre().contains(genre)
            && m.getRating() >= min_rating && m.getRuntime() <= max_runtime)
        .sorted(Comparator.comparing(e -> e.getTitle().replaceAll("^\"*|\"*$", "")))
        .collect(Collectors.toList());
    for (Movie m : movieList) {
      resultList.add(m.getTitle().replaceAll("^\"*|\"*$", ""));
    }

    return resultList;
  }

  static class Movie {

    private String poster_link;
    private String title;
    private Integer year;
    private String certificate;
    private Integer runtime;
    private String genre;
    private Float rating;
    private String overview;
    private Integer score;
    private String director;
    private String star1;
    private String star2;
    private String star3;
    private String star4;
    private Integer vote;
    private Integer gross;

    public Movie() {
    }

    @Override
    public String toString() {
      return "movie{" +
          "poster_link='" + poster_link + '\'' +
          ", title='" + title + '\'' +
          ", year='" + year + '\'' +
          ", certificate='" + certificate + '\'' +
          ", runtime='" + runtime + '\'' +
          ", genre='" + genre + '\'' +
          ", rating=" + rating +
          ", score=" + score +
          ", director='" + director + '\'' +
          ", star1='" + star1 + '\'' +
          ", star2='" + star2 + '\'' +
          ", star3='" + star3 + '\'' +
          ", star4='" + star4 + '\'' +
          ", vote=" + vote +
          ", gross=" + gross +
          '}';
    }

    public String getPoster_link() {
      return poster_link;
    }

    public void setPoster_link(String poster_link) {
      this.poster_link = poster_link;
    }

    public String getTitle() {
      return title;
    }

    public void setTitle(String title) {
      this.title = title;
    }

    public void setYear(Integer year) {
      this.year = year;
    }

    public void setRating(Float rating) {
      this.rating = rating;
    }

    public void setScore(Integer score) {
      this.score = score;
    }

    public void setVote(Integer vote) {
      this.vote = vote;
    }

    public void setGross(Integer gross) {
      this.gross = gross;
    }

    public Integer getYear() {
      return year;
    }


    public String getCertificate() {
      return certificate;
    }

    public void setCertificate(String certificate) {
      this.certificate = certificate;
    }

    public Integer getRuntime() {
      return runtime;
    }

    public void setRuntime(Integer runtime) {
      this.runtime = runtime;
    }

    public String getGenre() {
      return genre;
    }

    public void setGenre(String genre) {
      this.genre = genre;
    }

    public float getRating() {
      return rating;
    }

    public void setRating(float rating) {
      this.rating = rating;
    }

    public String getOverview() {
      return overview;
    }

    public void setOverview(String overview) {
      this.overview = overview;
    }

    public int getScore() {
      return score;
    }

    public void setScore(int score) {
      this.score = score;
    }

    public String getDirector() {
      return director;
    }

    public void setDirector(String director) {
      this.director = director;
    }

    public String getStar1() {
      return star1;
    }

    public void setStar1(String star1) {
      this.star1 = star1;
    }

    public String getStar2() {
      return star2;
    }

    public void setStar2(String star2) {
      this.star2 = star2;
    }

    public String getStar3() {
      return star3;
    }

    public void setStar3(String star3) {
      this.star3 = star3;
    }

    public String getStar4() {
      return star4;
    }

    public void setStar4(String star4) {
      this.star4 = star4;
    }

    public int getVote() {
      return vote;
    }

    public void setVote(int vote) {
      this.vote = vote;
    }

    public int getGross() {
      return gross;
    }

    public void setGross(int gross) {
      this.gross = gross;
    }
  }
}