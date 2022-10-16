import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class MovieAnalyzer {
    private  static Stream<movie> sentence;

    public MovieAnalyzer(String dataset_path){
        List<movie> movies = new ArrayList<>();
        try (BufferedReader infile
                     = new BufferedReader(new FileReader(dataset_path))){
            String   line;
            String[] parts;
            int cnt = 0;
            while ((line = infile.readLine()) != null){
                parts = line.split(",(?=([^\"]*\"[^\"]*\")*[^\"]*$)");
                if (parts.length <= 16 ){
                    if (cnt != 0) {
                        movie m = new movie();
                        m.title = parts[1];
                        if (parts[2].equals("")){
                            m.year = null;
                        }else {
                            m.year = Integer.parseInt(parts[2]);
                        }
                        m.certificate = parts[3];
                        m.runtime = parts[4];
                        m.genre = parts[5];
                        if (parts[6].equals("")){
                            m.rating = null;
                        }else {
                            m.rating = Float.parseFloat(parts[6]);
                        }
                        m.overview = parts[7];
                        if (parts[8].equals("")){
                            m.score = null;
                        }else {
                            m.score = Integer.parseInt(parts[8]);
                        }
                        m.director = parts[9];
                        m.star1 = parts[10];
                        m.star2 = parts[11];
                        m.star3 = parts[12];
                        m.star4 = parts[13];
                        if (parts[14].equals("")){
                            m.vote = null;
                        }else {
                            m.vote = Integer.parseInt(parts[14]);
                        }
                        if (parts.length == 15 || parts[15].equals("")){
                            m.gross = null;
                        }else {
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
        sentence = movies.stream();
    }



    public Map<Integer, Integer> getMovieCountByYear(){
        Map<Integer, Integer> resultMap = new LinkedHashMap<>();
        Map<Integer, Long> collectMap = sentence.collect(Collectors.groupingBy(movie::getYear, Collectors.counting()));
        collectMap.entrySet()
                .stream().sorted((v1,v2) -> {
                        if (v1.getKey().equals(v2.getKey())){
                            return v2.getValue().compareTo(v1.getValue());
                        }else {
                            return v2.getKey().compareTo(v1.getKey());
                        }
                }).forEachOrdered(e -> resultMap.put(e.getKey(), e.getValue().intValue()));
        return resultMap;
    }

    public static Map<String, Integer> getMovieCountByGenre(){
        Map<String, Integer> resultMap = new HashMap<>();
        sentence.forEach(item->{
            String genre = item.getGenre();
            String[] gList = genre.split(",");
            for (String g : gList){
                g = g.replace("\"", "");
                g = g.replace(" ","");
                if (!resultMap.containsKey(g)){
                    resultMap.put(g,1);
                }else {
                    resultMap.put(g,resultMap.get(g)+1);
                }
            }
        } );
        return resultMap;

    }

    public Map<List<String>, Integer> getCoStarCount(){
        return null;
    }

    public List<String> getTopMovies(int top_k, String by){
        return null;
    }

    public List<String> getTopStars(int top_k, String by){
        return null;
    }

    public List<String> searchMovies(String genre, float min_rating, int max_runtime){
        return null;
    }

     static class movie{
        String poster_link;
        String title;
        Integer year;
        String certificate;
        String runtime;
        String genre;
        Float rating;
        String overview;
        Integer score;
        String director;
        String star1;
        String star2;
        String star3;
        String star4;
        Integer vote;
        Integer gross;

        public movie() {
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

        public String getRuntime() {
            return runtime;
        }

        public void setRuntime(String runtime) {
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