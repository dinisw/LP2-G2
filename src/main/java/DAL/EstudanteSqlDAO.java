package DAL;

import model.Estudante;
import model.Resultado;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class EstudanteSqlDAO implements IEstudanteDAO {
    @Override
    public Resultado<Estudante> registarEstudante(Estudante estudante) {
        String sql = "INSERT INTO Estudantes (nome, morada, nif, data_nascimento, email, numero_mec, hash_senha, curso, ativo) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection connection = DatabaseConnection.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, estudante.getNome());
            statement.setString(2, estudante.getMorada());
            statement.setInt(3, estudante.getNif());
            statement.setDate(4, Date.valueOf(estudante.getDataNascimento()));
            statement.setString(5, estudante.getEmail());
            statement.setInt(6, estudante.getNumeroMec());
            statement.setString(7, estudante.getHash());
            statement.setString(8, estudante.getNomeCurso());
            statement.setBoolean(9, estudante.isAtivo());
            statement.executeUpdate();
            return new Resultado<>(estudante, true);
        } catch (SQLException e) {
            return new Resultado<>(false, "Erro SQL ao registar: " + e.getMessage());
        }
    }

    @Override
    public Estudante lerEstudante (int numeroMec) {
        String sql = "SELECT * FROM Estudantes WHERE numero_mec = ?";
        try (Connection connection = DatabaseConnection.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, numeroMec);
            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                return mapearResultSetParaEstudante(resultSet);
            }
        } catch (SQLException e) {
            System.err.println("Erro SQL: " + e.getMessage());
        }
        return null;
    }

    @Override
    public Resultado<Estudante> atualizarEstudante (Estudante estudante) {
        String sql = "UPDATE Estudantes SET nome=?, morada=?, nif=?, data_nascimento=?, email=?, hash_senha=?, curso=?, ativo=? WHERE numero_mec=?";        return new Resultado<>(estudante,true);
        try (Connection connection = DatabaseConnection.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, estudante.getNome());
            statement.setString(2, estudante.getMorada());
            statement.setInt(3, estudante.getNif());
            statement.setDate(4, Date.valueOf(estudante.getDataNascimento()));
            statement.setString(5, estudante.getEmail());
            statement.setString(6, estudante.getHash());
            statement.setString(7, estudante.getNomeCurso());
            statement.setBoolean(8, estudante.isAtivo());
            statement.setInt(9, estudante.getNumeroMec());

            statement.executeUpdate();
            return new Resultado<>(estudante, true);
        } catch (SQLException e) {
            return new Resultado<>(false, "Erro SQL ao atualizar: " + e.getMessage());
        }
    }

    @Override
    public Resultado<Estudante> atualizarSenha(Estudante estudante) {
        String sql = "UPDATE Estudantes SET hash_senha=? WHERE numero_mec=?";
        try (Connection connection = DatabaseConnection.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, estudante.getHash());
            statement.setInt(2, estudante.getNumeroMec());
            statement.executeUpdate();
            return new Resultado<>(estudante, true);
        } catch (SQLException e) {
            return new Resultado<>(false, "Erro SQL ao atualizar senha: " + e.getMessage());
        }
    }


    @Override
    public Resultado<Estudante> eliminarEstudante(int numeroMec) {
        String sql = "DELETE FROM Estudantes WHERE numero_mec=?";
        try (Connection connection = DatabaseConnection.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, numeroMec);
            int rowsAffected = statement.executeUpdate();
            if (rowsAffected > 0) {
                return new Resultado<>(null, true);
            } else {
                return new Resultado<>(false, "Estudante não encontrado na Base de Dados.");
            }
        } catch (SQLException e) {
            return new Resultado<>(false, "Erro SQL ao eliminar: " + e.getMessage());
        }
    }

    @Override
    public List<Estudante> getEstudantes() {
        List<Estudante> lista = new ArrayList<>();
        String sql = "SELECT * FROM Estudantes";
        try (Connection connection = DatabaseConnection.getConnection(); PreparedStatement statement = connection.prepareStatement(sql); ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                lista.add(mapearResultSetParaEstudante(resultSet));
            }
        } catch (SQLException e) {
            System.err.println("Erro SQL: " + e.getMessage());
        }
        return lista;
    }

    @Override
    public Estudante procurarPorNIF (int nif) {
        String sql = "SELECT * FROM Estudantes WHERE nif = ?";
        try (Connection connection = DatabaseConnection.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, nif);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                return mapearResultSetParaEstudante(resultSet);
            }
        } catch (SQLException e) {
            System.err.println("Erro SQL ao procurar NIF: " + e.getMessage());
        }
        return null;
    }

    @Override
    public int gerarNumeroMecanografico() {
        int anoAtual = java.time.LocalDate.now().getYear();
        int yy = anoAtual % 100;
        int prefixo = 1000000 + (yy * 10000);
        int baseComparacaoMin = prefixo;
        int baseComparacaoMax = prefixo + 9999;

        String sql = "SELECT MAX(numero_mec) as max_mec FROM Estudantes WHERE numero_mec >= ? AND numero_mec <= ?";

        try (Connection connection = DatabaseConnection.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, baseComparacaoMin);
            statement.setInt(2, baseComparacaoMax);
            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                int max = resultSet.getInt("max_mec");
                if (max > 0) return max + 1;
            }
        } catch (SQLException e) {
            System.err.println("Erro ao gerar Nº Mec: " + e.getMessage());
        }
        return prefixo + 1; 
    }

    private Estudante mapearResultSetParaEstudante(ResultSet rs) throws SQLException {
        return new Estudante(
                rs.getString("nome"),
                rs.getString("morada"),
                rs.getInt("nif"),
                rs.getDate("data_nascimento").toLocalDate(),
                rs.getString("email"),
                rs.getInt("numero_mec"),
                rs.getString("hash_senha"),
                rs.getString("curso"),
                rs.getBoolean("ativo")
        );
    }
}
