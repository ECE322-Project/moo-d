format: clean
	latexindent -w report.tex > /dev/null
	latexindent -w task1.tex > /dev/null
	latexindent -w task2.tex > /dev/null
	latexindent -w intro.tex > /dev/null
	latexindent -w conclusion.tex > /dev/null
clean:
	rm -rf output.html *.aux *.out *.fls *.fdb_latexmk *.log *.gz *.bak.* _minted-report *.bak*
report:
	pdflatex -shell-escape report
lint:
	textidote --check en_CA report.tex
lint-html:
	-textidote --output html --check en_CA report.tex > output.html
	-brave-dev output.html
