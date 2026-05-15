package com.example.ac2android;

import android.app.AlertDialog;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Collections;

public class MainActivity extends AppCompatActivity {

    EditText edtTitulo, edtAutor, edtAnoPublicacao;

    Spinner spGenero, spStatusLeitura, spFiltroGenero;

    CheckBox chkFavorito, chkSomenteFavoritos;

    Button btnSalvar;

    ListView listViewLivros;

    FirebaseFirestore db;

    ArrayAdapter<String> adapter;

    ArrayList<String> listaLivros;

    ArrayList<Livro> listaObjetosLivros;

    Livro livroEditando = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        edtTitulo = findViewById(R.id.edtTitulo);
        edtAutor = findViewById(R.id.edtAutor);
        edtAnoPublicacao = findViewById(R.id.edtAnoPublicacao);

        spGenero = findViewById(R.id.spGenero);
        spStatusLeitura = findViewById(R.id.spStatusLeitura);
        spFiltroGenero = findViewById(R.id.spFiltroGenero);

        chkFavorito = findViewById(R.id.chkFavorito);
        chkSomenteFavoritos = findViewById(R.id.chkSomenteFavoritos);

        btnSalvar = findViewById(R.id.btnSalvar);

        listViewLivros = findViewById(R.id.listViewLivros);

        db = FirebaseFirestore.getInstance();

        String[] generos = {
                "Romance",
                "Fantasia",
                "Terror",
                "Ficção Científica",
                "Biografia"
        };

        ArrayAdapter<String> adapterGenero = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                generos
        );

        adapterGenero.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item
        );

        spGenero.setAdapter(adapterGenero);

        String[] filtroGeneros = {
                "Todos",
                "Romance",
                "Fantasia",
                "Terror",
                "Ficção Científica",
                "Biografia"
        };

        ArrayAdapter<String> adapterFiltro = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                filtroGeneros
        );

        adapterFiltro.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item
        );

        spFiltroGenero.setAdapter(adapterFiltro);

        String[] status = {
                "Quero ler",
                "Lendo",
                "Concluído"
        };

        ArrayAdapter<String> adapterStatus = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                status
        );

        adapterStatus.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item
        );

        spStatusLeitura.setAdapter(adapterStatus);

        btnSalvar.setOnClickListener(v -> salvarLivro());

        spFiltroGenero.setOnItemSelectedListener(
                new android.widget.AdapterView.OnItemSelectedListener() {

                    @Override
                    public void onItemSelected(
                            android.widget.AdapterView<?> parent,
                            android.view.View view,
                            int position,
                            long id) {

                        carregarLivros();
                    }

                    @Override
                    public void onNothingSelected(
                            android.widget.AdapterView<?> parent) {

                    }
                });

        chkSomenteFavoritos.setOnCheckedChangeListener(
                (buttonView, isChecked) -> carregarLivros()
        );

        listViewLivros.setOnItemClickListener((parent, view, position, id) -> {

            if (position >= 0 && position < listaObjetosLivros.size()) {

                Livro livro = listaObjetosLivros.get(position);

                livroEditando = livro;

                edtTitulo.setText(livro.getTitulo());

                edtAutor.setText(livro.getAutor());

                edtAnoPublicacao.setText(
                        livro.getAnoPublicacao()
                );

                chkFavorito.setChecked(
                        livro.isFavorito()
                );

                ArrayAdapter<String> adapterGeneroLista =
                        (ArrayAdapter<String>) spGenero.getAdapter();

                int posGenero =
                        adapterGeneroLista.getPosition(
                                livro.getGenero()
                        );

                spGenero.setSelection(posGenero);

                ArrayAdapter<String> adapterStatusLista =
                        (ArrayAdapter<String>) spStatusLeitura.getAdapter();

                int posStatus =
                        adapterStatusLista.getPosition(
                                livro.getStatusLeitura()
                        );

                spStatusLeitura.setSelection(posStatus);

                btnSalvar.setText("Atualizar");
            }
        });

        listViewLivros.setOnItemLongClickListener(
                (parent, view, position, id) -> {

                    if (position >= 0
                            && position < listaObjetosLivros.size()) {

                        Livro livro =
                                listaObjetosLivros.get(position);

                        new AlertDialog.Builder(this)
                                .setTitle("Excluir livro")
                                .setMessage(
                                        "Deseja excluir este livro?"
                                )
                                .setPositiveButton(
                                        "Sim",
                                        (dialog, which) ->
                                                excluirLivro(livro)
                                )
                                .setNegativeButton(
                                        "Não",
                                        null
                                )
                                .show();
                    }

                    return true;
                });

        carregarLivros();
    }

    private void salvarLivro() {

        String titulo =
                edtTitulo.getText().toString().trim();

        String autor =
                edtAutor.getText().toString().trim();

        String genero =
                spGenero.getSelectedItem().toString();

        String anoPublicacao =
                edtAnoPublicacao.getText().toString().trim();

        String statusLeitura =
                spStatusLeitura.getSelectedItem().toString();

        boolean favorito =
                chkFavorito.isChecked();

        if (titulo.isEmpty()) {

            Toast.makeText(
                    this,
                    "O título não pode estar vazio!",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        if (autor.isEmpty()) {

            Toast.makeText(
                    this,
                    "O autor não pode estar vazio!",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        if (anoPublicacao.isEmpty()) {

            Toast.makeText(
                    this,
                    "O ano não pode estar vazio!",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        if (livroEditando == null) {

            Livro livro = new Livro(
                    null,
                    titulo,
                    autor,
                    genero,
                    anoPublicacao,
                    statusLeitura,
                    favorito
            );

            db.collection("livros")
                    .add(livro)
                    .addOnSuccessListener(documentReference -> {

                        Toast.makeText(
                                this,
                                "Livro salvo!",
                                Toast.LENGTH_SHORT
                        ).show();

                        limparCampos();

                        carregarLivros();
                    })
                    .addOnFailureListener(e -> {

                        Toast.makeText(
                                this,
                                "Erro ao salvar",
                                Toast.LENGTH_LONG
                        ).show();
                    });

        } else {

            livroEditando.setTitulo(titulo);

            livroEditando.setAutor(autor);

            livroEditando.setGenero(genero);

            livroEditando.setAnoPublicacao(
                    anoPublicacao
            );

            livroEditando.setStatusLeitura(
                    statusLeitura
            );

            livroEditando.setFavorito(favorito);

            db.collection("livros")
                    .document(livroEditando.getId())
                    .set(livroEditando)
                    .addOnSuccessListener(aVoid -> {

                        Toast.makeText(
                                this,
                                "Livro atualizado!",
                                Toast.LENGTH_SHORT
                        ).show();

                        limparCampos();

                        carregarLivros();
                    })
                    .addOnFailureListener(e -> {

                        Toast.makeText(
                                this,
                                "Erro ao atualizar",
                                Toast.LENGTH_LONG
                        ).show();
                    });
        }
    }

    private void carregarLivros() {

        db.collection("livros")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {

                    listaLivros = new ArrayList<>();

                    listaObjetosLivros = new ArrayList<>();

                    String filtroGenero =
                            spFiltroGenero
                                    .getSelectedItem()
                                    .toString();

                    boolean somenteFavoritos =
                            chkSomenteFavoritos.isChecked();

                    for (QueryDocumentSnapshot doc
                            : queryDocumentSnapshots) {

                        Livro livro =
                                doc.toObject(Livro.class);

                        livro.setId(doc.getId());

                        boolean passouGenero =
                                filtroGenero.equals("Todos")
                                        || livro.getGenero()
                                        .equals(filtroGenero);

                        boolean passouFavorito =
                                !somenteFavoritos
                                        || livro.isFavorito();

                        if (passouGenero
                                && passouFavorito) {

                            listaObjetosLivros.add(livro);
                        }
                    }

                    Collections.sort(
                            listaObjetosLivros,

                            (l1, l2) -> Integer.compare(
                                    Integer.parseInt(
                                            l1.getAnoPublicacao()
                                    ),

                                    Integer.parseInt(
                                            l2.getAnoPublicacao()
                                    )
                            )
                    );

                    for (Livro livro : listaObjetosLivros) {

                        String texto =
                                livro.getTitulo()
                                        + " | "
                                        + livro.getAutor()
                                        + " | "
                                        + livro.getGenero()
                                        + " | "
                                        + livro.getAnoPublicacao()
                                        + " | "
                                        + livro.getStatusLeitura();

                        if (livro.isFavorito()) {

                            texto += " | Favorito";
                        }

                        listaLivros.add(texto);
                    }

                    adapter = new ArrayAdapter<>(
                            this,
                            android.R.layout.simple_list_item_1,
                            listaLivros
                    );

                    listViewLivros.setAdapter(adapter);

                })
                .addOnFailureListener(e -> {

                    Toast.makeText(
                            this,
                            "Erro ao carregar livros",
                            Toast.LENGTH_LONG
                    ).show();
                });
    }

    private void excluirLivro(Livro livro) {

        db.collection("livros")
                .document(livro.getId())
                .delete()
                .addOnSuccessListener(aVoid -> {

                    Toast.makeText(
                            this,
                            "Livro excluído!",
                            Toast.LENGTH_SHORT
                    ).show();

                    limparCampos();

                    carregarLivros();
                })
                .addOnFailureListener(e -> {

                    Toast.makeText(
                            this,
                            "Erro ao excluir",
                            Toast.LENGTH_LONG
                    ).show();
                });
    }

    private void limparCampos() {

        edtTitulo.setText("");

        edtAutor.setText("");

        edtAnoPublicacao.setText("");

        chkFavorito.setChecked(false);

        spGenero.setSelection(0);

        spStatusLeitura.setSelection(0);

        livroEditando = null;

        btnSalvar.setText("Salvar");
    }
}