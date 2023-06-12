package MiniRedeSocial;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class Usuario {
    private String nome;
    private String email;
    private String senha;
    private List<Usuario> amigos;

    public Usuario(String nome, String email, String senha) {
        this.nome = nome;
        this.email = email;
        this.senha = senha;
        this.amigos = new ArrayList<>();
    }

    public String getNome() {
        return nome;
    }

    public String getEmail() {
        return email;
    }

    public String getSenha() {
        return senha;
    }

    public List<Usuario> getAmigos() {
        return amigos;
    }

    public void adicionarAmigo(Usuario amigo) {
        amigos.add(amigo);
        amigo.amigos.add(this);
    }

    public void removerAmigo(Usuario amigo) {
        amigos.remove(amigo);
        amigo.amigos.remove(this);
    }
}

class Sistema {
    private Map<String, Usuario> usuarios;
    private Usuario usuarioAtual;
    private Connection connection;

    public Sistema() {
        this.usuarios = new HashMap<>();
        this.usuarioAtual = null;
        this.connection = null;
    }

    public void conectarBancoDados() {
        String url = "jdbc:postgresql://localhost/acai_Mania";
        String username = "postgres";
        String password = "123456";
        this.connection = connection;
        try {
            connection = DriverManager.getConnection(url, username, password);
            System.out.println("Conexão com o banco de dados estabelecida.");
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("Erro ao conectar ao banco de dados.");
        }
    }

    public void desconectarBancoDados() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                System.out.println("Conexão com o banco de dados encerrada.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("Erro ao desconectar do banco de dados.");
        }
    }

    public void cadastrarUsuario(String nome, String email, String senha) {
        try {
            PreparedStatement statement = this.connection.prepareStatement( // Correção: usar this.connection
                    "INSERT INTO usuarios (nome, email, senha) VALUES (?, ?, ?)");
            statement.setString(1, nome);
            statement.setString(2, email);
            statement.setString(3, senha);
            statement.executeUpdate();
            statement.close();
            System.out.println("Usuário cadastrado com sucesso!");
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("Erro ao cadastrar usuário.");
        }
    }


    public void login(String email, String senha) {
        try {
            PreparedStatement statement = this.connection.prepareStatement( // Correção: usar this.connection
                    "SELECT * FROM usuarios WHERE email = ? AND senha = ?");
            statement.setString(1, email);
            statement.setString(2, senha);
            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                String nome = resultSet.getString("nome");
                Usuario usuario = new Usuario(nome, email, senha);
                usuarios.put(email, usuario);
                usuarioAtual = usuario;
                System.out.println("Login efetuado com sucesso!");
            } else {
                System.out.println("Email ou senha incorretos. Tente novamente.");
            }

            statement.close();
            resultSet.close();
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("Erro ao efetuar login.");
        }
    }
    public void logout() {
        usuarioAtual = null;
        System.out.println("Logout efetuado com sucesso!");
    }

    public void incluirAmigo(String emailAmigo) {
        if (usuarioAtual == null) {
            System.out.println("Faça login para acessar essa opção.");
            return;
        }

        Usuario amigo = usuarios.get(emailAmigo);
        if (amigo != null && amigo != usuarioAtual) {
            usuarioAtual.adicionarAmigo(amigo);
            System.out.println("Amigo adicionado com sucesso!");
        } else {
            System.out.println("Amigo não encontrado. Verifique o email digitado.");
        }
    }

    public void consultarAmigos() {
        if (usuarioAtual == null) {
            System.out.println("Faça login para acessar essa opção.");
            return;
        }

        List<Usuario> amigos = usuarioAtual.getAmigos();
        if (amigos.isEmpty()) {
            System.out.println("Você não possui amigos adicionados.");
        } else {
            System.out.println("Amigos:");
            for (Usuario amigo : amigos) {
                System.out.println("- " + amigo.getNome());
            }
        }
    }

    public void excluirAmigo(String emailAmigo) {
        if (usuarioAtual == null) {
            System.out.println("Faça login para acessar essa opção.");
            return;
        }

        Usuario amigo = usuarios.get(emailAmigo);
        if (amigo != null && usuarioAtual.getAmigos().contains(amigo)) {
            usuarioAtual.removerAmigo(amigo);
            System.out.println("Amigo removido com sucesso!");
        } else {
            System.out.println("Amigo não encontrado. Verifique o email digitado.");
        }
    }

    public void enviarMensagem(String emailAmigo, String mensagem) {
        if (usuarioAtual == null) {
            System.out.println("Faça login para acessar essa opção.");
            return;
        }

        Usuario amigo = usuarios.get(emailAmigo);
        if (amigo != null && usuarioAtual.getAmigos().contains(amigo)) {
            System.out.println("Mensagem enviada para " + amigo.getNome() + ": " + mensagem);
        } else {
            System.out.println("Amigo não encontrado. Verifique o email digitado.");
        }
    }

    public void exibirMenu() {
        System.out.println("== Mini Simulador de Rede Social ==");
        System.out.println("Selecione uma opção:");
        System.out.println("1. Cadastrar Usuário");
        System.out.println("2. Login");
        System.out.println("3. Logout");
        System.out.println("4. Incluir Amigo");
        System.out.println("5. Consultar Amigos");
        System.out.println("6. Excluir Amigo");
        System.out.println("7. Enviar Mensagem");
        System.out.println("0. Sair");
        System.out.print("Opção: ");
    }
}

public class MiniSimuladorRedeSocial {
    private Sistema sistema;
    private JFrame frame;
    private JTextField tfNome;
    private JTextField tfEmail;
    private JPasswordField pfSenha;
    private JTextField tfEmailLogin;
    private JPasswordField pfSenhaLogin;
    private JTextField tfEmailAmigo;
    private JTextField tfEmailAmigoExcluir;
    private JTextField tfEmailAmigoMensagem;
    private JTextArea taMensagem;

    public MiniSimuladorRedeSocial() {
        sistema = new Sistema();
        sistema.conectarBancoDados();
        criarGUI();
    }

    private void criarGUI() {
        frame = new JFrame("Mini Simulador de Rede Social");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(400, 300);
        frame.setLayout(new BorderLayout());

        JPanel panelCadastro = criarPainelCadastro();
        JPanel panelLogin = criarPainelLogin();
        JPanel panelOpcoes = criarPainelOpcoes();

        frame.add(panelCadastro, BorderLayout.NORTH);
        frame.add(panelLogin, BorderLayout.CENTER);
        frame.add(panelOpcoes, BorderLayout.SOUTH);

        frame.setVisible(true);
    }

    private JPanel criarPainelCadastro() {
        JPanel panelCadastro = new JPanel();
        panelCadastro.setLayout(new GridLayout(3, 2));

        JLabel lblNome = new JLabel("Nome:");
        JLabel lblEmail = new JLabel("Email:");
        JLabel lblSenha = new JLabel("Senha:");

        tfNome = new JTextField();
        tfEmail = new JTextField();
        pfSenha = new JPasswordField();

        panelCadastro.add(lblNome);
        panelCadastro.add(tfNome);
        panelCadastro.add(lblEmail);
        panelCadastro.add(tfEmail);
        panelCadastro.add(lblSenha);
        panelCadastro.add(pfSenha);

        return panelCadastro;
    }

    private JPanel criarPainelLogin() {
        JPanel panelLogin = new JPanel();
        panelLogin.setLayout(new GridLayout(2, 2));

        JLabel lblEmailLogin = new JLabel("Email:");
        JLabel lblSenhaLogin = new JLabel("Senha:");

        tfEmailLogin = new JTextField();
        pfSenhaLogin = new JPasswordField();

        panelLogin.add(lblEmailLogin);
        panelLogin.add(tfEmailLogin);
        panelLogin.add(lblSenhaLogin);
        panelLogin.add(pfSenhaLogin);

        return panelLogin;
    }

    private JPanel criarPainelOpcoes() {
        JPanel panelOpcoes = new JPanel();
        panelOpcoes.setLayout(new GridLayout(4, 2));

        JButton btnCadastrar = new JButton("Cadastrar");
        JButton btnLogin = new JButton("Login");
        JButton btnLogout = new JButton("Logout");
        JButton btnIncluirAmigo = new JButton("Incluir Amigo");
        JButton btnConsultarAmigos = new JButton("Consultar Amigos");
        JButton btnExcluirAmigo = new JButton("Excluir Amigo");
        JButton btnEnviarMensagem = new JButton("Enviar Mensagem");

        tfEmailAmigo = new JTextField();
        tfEmailAmigoExcluir = new JTextField();
        tfEmailAmigoMensagem = new JTextField();
        taMensagem = new JTextArea();

        panelOpcoes.add(btnCadastrar);
        panelOpcoes.add(btnLogin);
        panelOpcoes.add(btnLogout);
        panelOpcoes.add(btnIncluirAmigo);
        panelOpcoes.add(btnConsultarAmigos);
        panelOpcoes.add(btnExcluirAmigo);
        panelOpcoes.add(btnEnviarMensagem);

        panelOpcoes.add(new JLabel("Email do Amigo:"));
        panelOpcoes.add(tfEmailAmigo);
        panelOpcoes.add(new JLabel("Email do Amigo para Excluir:"));
        panelOpcoes.add(tfEmailAmigoExcluir);
        panelOpcoes.add(new JLabel("Email do Amigo para Enviar Mensagem:"));
        panelOpcoes.add(tfEmailAmigoMensagem);
        panelOpcoes.add(new JLabel("Mensagem:"));
        panelOpcoes.add(taMensagem);

        btnCadastrar.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                cadastrarUsuario();
            }
        });

        btnLogin.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                login();
            }
        });

        btnLogout.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                logout();
            }
        });

        btnIncluirAmigo.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                incluirAmigo();
            }
        });

        btnConsultarAmigos.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                consultarAmigos();
            }
        });

        btnExcluirAmigo.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                excluirAmigo();
            }
        });

        btnEnviarMensagem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                enviarMensagem();
            }
        });

        return panelOpcoes;
    }

    private void cadastrarUsuario() {
        String nome = tfNome.getText();
        String email = tfEmail.getText();
        String senha = new String(pfSenha.getPassword());

        sistema.cadastrarUsuario(nome, email, senha);

        tfNome.setText("");
        tfEmail.setText("");
        pfSenha.setText("");
    }

    private void login() {
        String email = tfEmailLogin.getText();
        String senha = new String(pfSenhaLogin.getPassword());

        sistema.login(email, senha);

        tfEmailLogin.setText("");
        pfSenhaLogin.setText("");
    }

    private void logout() {
        sistema.logout();
    }

    private void incluirAmigo() {
        String emailAmigo = tfEmailAmigo.getText();
        sistema.incluirAmigo(emailAmigo);
        tfEmailAmigo.setText("");
    }

    private void consultarAmigos() {
        sistema.consultarAmigos();
    }

    private void excluirAmigo() {
        String emailAmigo = tfEmailAmigoExcluir.getText();
        sistema.excluirAmigo(emailAmigo);
        tfEmailAmigoExcluir.setText("");
    }

    private void enviarMensagem() {
        String emailAmigo = tfEmailAmigoMensagem.getText();
        String mensagem = taMensagem.getText();
        sistema.enviarMensagem(emailAmigo, mensagem);
        tfEmailAmigoMensagem.setText("");
        taMensagem.setText("");
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new MiniSimuladorRedeSocial();
            }
        });
    }
}
